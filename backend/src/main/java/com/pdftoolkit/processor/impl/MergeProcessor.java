package com.pdftoolkit.processor.impl;

import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessResult;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * Merges all input PDFs into one, preserving input order.
 */
@Component
public class MergeProcessor implements FileProcessor {

    @Override
    public OperationType type() {
        return OperationType.MERGE;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        if (context.inputs().size() < 2) {
            throw new ProcessingException("Merge requires at least two PDF files");
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        merger.setDestinationStream(out);
        for (ProcessInput input : context.inputs()) {
            merger.addSource(input.path().toFile());
        }
        merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

        return ProcessResult.single(
                new ProcessOutput("merged.pdf", "application/pdf", out.toByteArray()));
    }
}
