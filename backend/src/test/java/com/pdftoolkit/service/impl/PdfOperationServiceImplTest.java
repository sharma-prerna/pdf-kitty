package com.pdftoolkit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.entity.ProcessingJob;
import com.pdftoolkit.exception.FileValidationException;
import com.pdftoolkit.exception.UnsupportedFileTypeException;
import com.pdftoolkit.mapper.JobMapper;
import com.pdftoolkit.queue.JobQueue;
import com.pdftoolkit.repository.ProcessingJobRepository;
import com.pdftoolkit.repository.UploadedFileRepository;
import com.pdftoolkit.storage.StorageService;
import com.pdftoolkit.util.FileValidationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfOperationServiceImplTest {

    @Mock ProcessingJobRepository jobRepository;
    @Mock UploadedFileRepository uploadedFileRepository;
    @Mock StorageService storageService;
    @Mock FileValidationUtil fileValidationUtil;
    @Mock JobQueue jobQueue;
    @Mock JobMapper jobMapper;
    @Spy ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks PdfOperationServiceImpl service;

    private MultipartFile pdf(String name) {
        return new MockMultipartFile("files", name, "application/pdf", new byte[]{1, 2, 3});
    }

    @Test
    void submitCreatesQueuedJobAndEnqueues() {
        when(fileValidationUtil.validate(any())).thenReturn("application/pdf");
        when(storageService.storeUpload(any(), any(), anyString())).thenReturn("key");

        service.submit(OperationType.COMPRESS, List.of(pdf("a.pdf")), Map.of("level", "HIGH"));

        ArgumentCaptor<ProcessingJob> jobCaptor = ArgumentCaptor.forClass(ProcessingJob.class);
        verify(jobRepository, atLeastOnce()).save(jobCaptor.capture());
        ProcessingJob saved = jobCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(saved.getOperationType()).isEqualTo(OperationType.COMPRESS);
        verify(jobQueue).submit(eq(saved.getId()));
    }

    @Test
    void mergeRequiresAtLeastTwoFiles() {
        assertThatThrownBy(() -> service.submit(OperationType.MERGE, List.of(pdf("a.pdf")), Map.of()))
                .isInstanceOf(FileValidationException.class);
    }

    @Test
    void rejectsWrongExtensionForOperation() {
        MultipartFile docx = new MockMultipartFile("files", "a.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[]{1});
        when(fileValidationUtil.validate(any())).thenReturn(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThatThrownBy(() -> service.submit(OperationType.COMPRESS, List.of(docx), Map.of()))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }
}
