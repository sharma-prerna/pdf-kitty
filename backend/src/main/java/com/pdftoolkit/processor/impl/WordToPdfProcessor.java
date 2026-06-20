package com.pdftoolkit.processor.impl;

import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessResult;
import lombok.RequiredArgsConstructor;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Converts a Word document (.doc/.docx) to PDF using a headless LibreOffice process pool
 * (managed by JODConverter), preserving fonts, images, tables, headers/footers and layout.
 *
 * <p>The {@link DocumentConverter} is provided only when the local office manager is enabled
 * (see {@code jodconverter.local.enabled}); it is disabled by default so the application boots
 * in environments without LibreOffice installed (local dev, tests). When unavailable, a job
 * for this operation fails with a clear message rather than silently degrading to text-only.
 */
@Component
@RequiredArgsConstructor
public class WordToPdfProcessor implements FileProcessor {

    private final ObjectProvider<DocumentConverter> converterProvider;

    @Override
    public OperationType type() {
        return OperationType.WORD_TO_PDF;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        DocumentConverter converter = converterProvider.getIfAvailable();
        if (converter == null) {
            throw new ProcessingException(
                    "Word to PDF conversion is unavailable: LibreOffice is not configured in this environment");
        }

        ProcessInput input = context.firstInput();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Input format is inferred from the file extension; output is forced to PDF
            // because the target stream carries no name to infer from.
            converter.convert(input.path().toFile())
                    .to(out)
                    .as(DefaultDocumentFormatRegistry.PDF)
                    .execute();

            String outName = baseName(input.fileName()) + ".pdf";

            return ProcessResult.single(
                    new ProcessOutput(outName, "application/pdf", out.toByteArray()));

        } catch (Exception e) {
            throw new ProcessingException("Failed to convert document to PDF", e);
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
