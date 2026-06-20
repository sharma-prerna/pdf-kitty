package com.pdftoolkit.processor.impl;

import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MergeProcessorTest {

    private final MergeProcessor processor = new MergeProcessor();

    @Test
    void mergesAllPagesInOrder(@TempDir Path dir) throws Exception {
        Path a = TestFiles.pdfFile(dir, "a.pdf", 2);
        Path b = TestFiles.pdfFile(dir, "b.pdf", 3);
        ProcessContext context = new ProcessContext(UUID.randomUUID(), List.of(
                new ProcessInput("a.pdf", a, "application/pdf"),
                new ProcessInput("b.pdf", b, "application/pdf")), Map.of());

        ProcessResult result = processor.process(context);

        assertThat(result.outputs()).hasSize(1);
        try (PDDocument merged = Loader.loadPDF(result.outputs().get(0).content())) {
            assertThat(merged.getNumberOfPages()).isEqualTo(5);
        }
    }

    @Test
    void rejectsSingleInput(@TempDir Path dir) throws Exception {
        Path a = TestFiles.pdfFile(dir, "a.pdf", 1);
        ProcessContext context = new ProcessContext(UUID.randomUUID(),
                List.of(new ProcessInput("a.pdf", a, "application/pdf")), Map.of());

        assertThatThrownBy(() -> processor.process(context))
                .isInstanceOf(ProcessingException.class);
    }

    @Test
    void reportsCorrectType() {
        assertThat(processor.type()).isEqualTo(OperationType.MERGE);
    }
}
