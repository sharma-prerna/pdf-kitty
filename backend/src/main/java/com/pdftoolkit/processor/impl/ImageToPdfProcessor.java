package com.pdftoolkit.processor.impl;

import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Combines images into a single PDF, one page per image (input order preserved). Each page is
 * sized to its image and the image is scaled to fit within a max page dimension.
 */
@Component
public class ImageToPdfProcessor implements FileProcessor {

    /** Cap on a page side, in points, so very large images don't create oversized pages. */
    private static final float MAX_SIDE = PDRectangle.A4.getHeight() * 2;

    @Override
    public OperationType type() {
        return OperationType.IMAGE_TO_PDF;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        if (context.inputs().isEmpty()) {
            throw new ProcessingException("Image-to-PDF requires at least one image");
        }

        try (PDDocument document = new PDDocument()) {
            for (ProcessInput input : context.inputs()) {
                PDImageXObject image = PDImageXObject.createFromFileByExtension(
                        input.path().toFile(), document);
                addImagePage(document, image);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return ProcessResult.single(
                    new ProcessOutput("images.pdf", "application/pdf", out.toByteArray()));
        }
    }

    private void addImagePage(PDDocument document, PDImageXObject image) throws Exception {
        float width = image.getWidth();
        float height = image.getHeight();
        float scale = Math.min(1f, MAX_SIDE / Math.max(width, height));
        float pageWidth = width * scale;
        float pageHeight = height * scale;

        PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
        document.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            content.drawImage(image, 0, 0, pageWidth, pageHeight);
        }
    }
}
