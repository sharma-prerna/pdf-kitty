package com.pdftoolkit.processor.impl;

import com.pdftoolkit.dto.CompressionLevel;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.processor.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compresses a PDF by re-encoding embedded raster images at a reduced resolution and JPEG
 * quality (per {@link CompressionLevel}), stripping metadata, and re-saving with object-stream
 * compression. Text remains selectable. Images with no raster content are left untouched.
 */
@Slf4j
@Component
public class CompressProcessor implements FileProcessor {

    /** Skip images smaller than this many pixels on their longest side. */
    private static final int MIN_SIDE_TO_COMPRESS = 200;
    private static final int MAX_FORM_RECURSION = 12;

    @Override
    public OperationType type() {
        return OperationType.COMPRESS;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        ProcessInput input = context.firstInput();
        CompressionLevel level = CompressionLevel.valueOf(
                context.param(ProcessParams.COMPRESSION_LEVEL).orElse("MEDIUM").toUpperCase(Locale.ROOT));
        float scale = scaleFor(level);

        try (PDDocument document = Loader.loadPDF(input.path().toFile())) {
            for (PDPage page : document.getPages()) {
                compressResources(document, page.getResources(), level, scale, 0);
            }
            stripMetadata(document);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            String outName = baseName(input.fileName()) + "-compressed.pdf";
            return ProcessResult.single(new ProcessOutput(outName, "application/pdf", out.toByteArray()));
        }
    }

    private void compressResources(PDDocument document, PDResources resources,
                                   CompressionLevel level, float scale, int depth) {
        if (resources == null || depth > MAX_FORM_RECURSION) {
            return;
        }
        // Copy names first: getXObjectNames() is a live view and we mutate resources via put().
        List<COSName> names = new ArrayList<>();
        resources.getXObjectNames().forEach(names::add);
        for (COSName name : names) {
            try {
                PDXObject xObject = resources.getXObject(name);
                if (xObject instanceof PDImageXObject image) {
                    PDImageXObject replacement = recompress(document, image, level, scale);
                    if (replacement != null) {
                        resources.put(name, replacement);
                    }
                } else if (xObject instanceof PDFormXObject form) {
                    compressResources(document, form.getResources(), level, scale, depth + 1);
                }
            } catch (Exception e) {
                // A single problematic image must not abort the whole document.
                log.debug("Skipping image {}: {}", name.getName(), e.getMessage());
            }
        }
    }

    private PDImageXObject recompress(PDDocument document, PDImageXObject image,
                                      CompressionLevel level, float scale) throws Exception {
        BufferedImage source = image.getImage();
        if (source == null) {
            return null;
        }
        int longest = Math.max(source.getWidth(), source.getHeight());
        if (longest < MIN_SIDE_TO_COMPRESS) {
            return null;
        }

        int newWidth = Math.max(1, Math.round(source.getWidth() * scale));
        int newHeight = Math.max(1, Math.round(source.getHeight() * scale));
        BufferedImage rgb = toScaledRgb(source, newWidth, newHeight);
        return JPEGFactory.createFromImage(document, rgb, level.jpegQuality());
    }

    /** Scale and flatten onto an opaque RGB raster so the result is JPEG-encodable. */
    private BufferedImage toScaledRgb(BufferedImage source, int width, int height) {
        BufferedImage rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return rgb;
    }

    private void stripMetadata(PDDocument document) {
        document.setDocumentInformation(new PDDocumentInformation());
        document.getDocumentCatalog().setMetadata(null);
    }

    private float scaleFor(CompressionLevel level) {
        return switch (level) {
            case LOW -> 0.85f;
            case MEDIUM -> 0.7f;
            case HIGH -> 0.5f;
        };
    }

    private String baseName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }
}
