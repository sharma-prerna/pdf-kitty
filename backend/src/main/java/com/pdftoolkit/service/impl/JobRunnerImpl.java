package com.pdftoolkit.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdftoolkit.config.AsyncConfig;
import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OutputFile;
import com.pdftoolkit.entity.ProcessingJob;
import com.pdftoolkit.entity.UploadedFile;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessResult;
import com.pdftoolkit.processor.ProcessorRegistry;
import com.pdftoolkit.queue.JobRunner;
import com.pdftoolkit.repository.OutputFileRepository;
import com.pdftoolkit.repository.ProcessingJobRepository;
import com.pdftoolkit.repository.UploadedFileRepository;
import com.pdftoolkit.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Asynchronous worker that drives a job through PROCESSING → COMPLETED/FAILED. Status updates
 * are committed incrementally so polling clients observe progress in real time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobRunnerImpl implements JobRunner {

    private static final int MAX_ERROR_LENGTH = 1900;

    private final ProcessingJobRepository jobRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final OutputFileRepository outputFileRepository;
    private final StorageService storageService;
    private final ProcessorRegistry processorRegistry;
    private final ObjectMapper objectMapper;

    @Override
    @Async(AsyncConfig.JOB_EXECUTOR)
    public void run(UUID jobId) {
        ProcessingJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Job {} no longer exists; skipping", jobId);
            return;
        }

        job.setStatus(JobStatus.PROCESSING);
        job.setProcessingStartedAt(Instant.now());
        jobRepository.save(job);
        log.info("Processing job {} ({})", jobId, job.getOperationType());

        try {
            ProcessResult result = execute(job);
            persistOutputs(jobId, result);

            job.setStatus(JobStatus.COMPLETED);
            job.setOutputFileName(outputName(result));
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
            log.info("Completed job {} with {} output(s)", jobId, result.outputs().size());
        } catch (Exception e) {
            log.error("Job {} failed", jobId, e);
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(truncate(e.getMessage()));
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    private ProcessResult execute(ProcessingJob job) throws Exception {
        List<UploadedFile> uploads = uploadedFileRepository.findByJobIdOrderBySortOrderAsc(job.getId());
        List<ProcessInput> inputs = uploads.stream()
                .map(u -> new ProcessInput(u.getFileName(), storageService.resolve(u.getFilePath()), u.getMimeType()))
                .toList();

        ProcessContext context = new ProcessContext(job.getId(), inputs, parseParams(job.getParameters()));
        FileProcessor processor = processorRegistry.get(job.getOperationType());
        return processor.process(context);
    }

    private void persistOutputs(UUID jobId, ProcessResult result) {
        List<ProcessOutput> outputs = result.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            ProcessOutput output = outputs.get(i);
            String key = storageService.storeOutput(jobId, output.content(), output.fileName());
            outputFileRepository.save(OutputFile.builder()
                    .id(UUID.randomUUID())
                    .jobId(jobId)
                    .fileName(output.fileName())
                    .filePath(key)
                    .fileSize(output.content().length)
                    .mimeType(output.mimeType())
                    .sortOrder(i)
                    .build());
        }
    }

    private String outputName(ProcessResult result) {
        if (result.outputs().size() == 1) {
            return result.outputs().get(0).fileName();
        }
        return "results.zip";
    }

    private Map<String, String> parseParams(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.warn("Could not parse job parameters '{}': {}", json, e.getMessage());
            return Map.of();
        }
    }

    private String truncate(String message) {
        if (message == null) {
            return "Processing failed";
        }
        return message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message;
    }
}
