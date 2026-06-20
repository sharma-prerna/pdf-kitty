package com.pdftoolkit.processor.impl;

import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.processor.ProcessResult;
import com.pdftoolkit.support.TestFiles;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImageAndConversionProcessorTest {

    @Test
    void imageToPdfProducesOnePagePerImage(@TempDir Path dir) throws Exception {
        Path img1 = TestFiles.pngFile(dir, "1.png", 300, 200);
        Path img2 = TestFiles.pngFile(dir, "2.png", 200, 300);
        ProcessContext context = new ProcessContext(UUID.randomUUID(), List.of(
                new ProcessInput("1.png", img1, "image/png"),
                new ProcessInput("2.png", img2, "image/png")), Map.of());

        ProcessResult result = new ImageToPdfProcessor().process(context);

        try (PDDocument doc = Loader.loadPDF(result.outputs().get(0).content())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(2);
        }
    }

    @Test
    void pdfToImageRendersOneImagePerPage(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 2);
        ProcessContext context = new ProcessContext(UUID.randomUUID(),
                List.of(new ProcessInput("in.pdf", pdf, "application/pdf")),
                Map.of(ProcessParams.IMAGE_FORMAT, "PNG", ProcessParams.DPI, "72"));

        ProcessResult result = new PdfToImageProcessor().process(context);

        assertThat(result.outputs()).hasSize(2);
        assertThat(result.outputs().get(0).mimeType()).isEqualTo("image/png");
    }

    @Test
    void pdfToWordExtractsText(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 1);
        ProcessContext context = new ProcessContext(UUID.randomUUID(),
                List.of(new ProcessInput("in.pdf", pdf, "application/pdf")), Map.of());

        ProcessResult result = new PdfToWordProcessor().process(context);

        try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(result.outputs().get(0).content()))) {
            String text = docx.getParagraphs().stream().map(p -> p.getText()).reduce("", String::concat);
            assertThat(text).contains("Sample page 1");
        }
    }

    @Test
    void compressProducesValidPdf(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 2);
        ProcessContext context = new ProcessContext(UUID.randomUUID(),
                List.of(new ProcessInput("in.pdf", pdf, "application/pdf")),
                Map.of(ProcessParams.COMPRESSION_LEVEL, "HIGH"));

        ProcessResult result = new CompressProcessor().process(context);

        try (PDDocument doc = Loader.loadPDF(result.outputs().get(0).content())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(2);
        }
    }
}
