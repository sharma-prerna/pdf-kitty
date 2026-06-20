package com.pdftoolkit.processor.impl;

import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Extracts text from a PDF (PDFBox) and writes it into a DOCX (POI XWPF).
 *
 * <p><b>Limitation:</b> this is a pure-Java, text-only conversion — page layout, images, and
 * complex formatting are not preserved. Each PDF page becomes a block of paragraphs.
 */
@Component
public class PdfToWordProcessor implements FileProcessor {

    @Override
    public OperationType type() {
        return OperationType.PDF_TO_WORD;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        ProcessInput input = context.firstInput();
        try (PDDocument pdf = Loader.loadPDF(input.path().toFile());
             XWPFDocument docx = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int pageCount = pdf.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(pdf);
                writePage(docx, text);
                if (page < pageCount) {
                    docx.createParagraph().setPageBreak(true);
                }
            }

            docx.write(out);
            String outName = baseName(input.fileName()) + ".docx";
            return ProcessResult.single(new ProcessOutput(
                    outName,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    out.toByteArray()));
        } catch (Exception e) {
            throw new ProcessingException("Failed to convert PDF to DOCX: " + e.getMessage(), e);
        }
    }

    private void writePage(XWPFDocument docx, String text) {
        for (String line : text.split("\\R", -1)) {
            XWPFParagraph paragraph = docx.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(line);
        }
    }

    private String baseName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "document";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }
}
