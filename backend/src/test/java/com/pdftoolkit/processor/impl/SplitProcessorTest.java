package com.pdftoolkit.processor.impl;

import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.processor.ProcessResult;
import com.pdftoolkit.support.TestFiles;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SplitProcessorTest {

    private final SplitProcessor processor = new SplitProcessor();

    private ProcessContext context(Path pdf, Map<String, String> params) {
        return new ProcessContext(UUID.randomUUID(),
                List.of(new ProcessInput("in.pdf", pdf, "application/pdf")), params);
    }

    @Test
    void everyPageBecomesOneDocument(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 3);
        ProcessResult result = processor.process(context(pdf, Map.of(ProcessParams.SPLIT_MODE, "EVERY")));
        assertThat(result.outputs()).hasSize(3);
    }

    @Test
    void rangeProducesSingleDocumentWithRange(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 5);
        ProcessResult result = processor.process(context(pdf,
                Map.of(ProcessParams.SPLIT_MODE, "RANGE", ProcessParams.SPLIT_SPEC, "2-4")));
        assertThat(result.outputs()).hasSize(1);
        try (PDDocument doc = Loader.loadPDF(result.outputs().get(0).content())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(3);
        }
    }

    @Test
    void pagesListProducesOneDocumentPerPage(@TempDir Path dir) throws Exception {
        Path pdf = TestFiles.pdfFile(dir, "in.pdf", 5);
        ProcessResult result = processor.process(context(pdf,
                Map.of(ProcessParams.SPLIT_MODE, "PAGES", ProcessParams.SPLIT_SPEC, "1,3,5")));
        assertThat(result.outputs()).hasSize(3);
    }
}
