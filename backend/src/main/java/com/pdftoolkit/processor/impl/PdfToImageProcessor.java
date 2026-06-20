package com.pdftoolkit.processor.impl;

import com.pdftoolkit.dto.ImageFormat;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.processor.ProcessResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renders each PDF page to a raster image at the requested format and DPI.
 */
@Component
public class PdfToImageProcessor implements FileProcessor {

    private static final int MIN_DPI = 36;
    private static final int MAX_DPI = 600;

    @Override
    public OperationType type() {
        return OperationType.PDF_TO_IMAGE;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        ProcessInput input = context.firstInput();
        ImageFormat format = ImageFormat.valueOf(
                context.param(ProcessParams.IMAGE_FORMAT).orElse("PNG").toUpperCase(Locale.ROOT));
        int dpi = clampDpi(context.intParam(ProcessParams.DPI, 150));

        try (PDDocument document = Loader.loadPDF(input.path().toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            ImageType imageType = format == ImageFormat.JPG ? ImageType.RGB : ImageType.ARGB;
            List<ProcessOutput> outputs = new ArrayList<>();
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, dpi, imageType);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (!ImageIO.write(image, format.extension(), out)) {
                    throw new IllegalStateException("No image writer for format " + format);
                }
                String name = "page-%d.%s".formatted(page + 1, format.extension());
                outputs.add(new ProcessOutput(name, format.mimeType(), out.toByteArray()));
            }
            return new ProcessResult(outputs);
        }
    }

    private int clampDpi(int dpi) {
        return Math.max(MIN_DPI, Math.min(MAX_DPI, dpi));
    }
}
