package com.pdftoolkit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdftoolkit.dto.FileMetadataResponse;
import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.entity.ProcessingJob;
import com.pdftoolkit.entity.UploadedFile;
import com.pdftoolkit.exception.FileValidationException;
import com.pdftoolkit.exception.UnsupportedFileTypeException;
import com.pdftoolkit.mapper.JobMapper;
import com.pdftoolkit.queue.JobQueue;
import com.pdftoolkit.repository.ProcessingJobRepository;
import com.pdftoolkit.repository.UploadedFileRepository;
import com.pdftoolkit.service.PdfOperationService;
import com.pdftoolkit.storage.StorageService;
import com.pdftoolkit.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates job creation: validate uploads, persist the job and its inputs, then enqueue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfOperationServiceImpl implements PdfOperationService {

    /** Allowed input extensions per operation. */
    private static final Map<OperationType, Set<String>> ALLOWED = Map.of(
            OperationType.COMPRESS, Set.of("pdf"),
            OperationType.MERGE, Set.of("pdf"),
            OperationType.SPLIT, Set.of("pdf"),
            OperationType.PDF_TO_WORD, Set.of("pdf"),
            OperationType.PDF_TO_IMAGE, Set.of("pdf"),
            OperationType.IMAGE_TO_PDF, Set.of("jpg", "jpeg", "png"));

    private static final Set<OperationType> MULTI_FILE = Set.of(OperationType.MERGE, OperationType.IMAGE_TO_PDF);

    private final ProcessingJobRepository jobRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final StorageService storageService;
    private final FileValidationUtil fileValidationUtil;
    private final JobQueue jobQueue;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<FileMetadataResponse> validateOnly(List<MultipartFile> files) {
        requireNonEmpty(files);
        List<FileMetadataResponse> result = new ArrayList<>();
        for (MultipartFile file : files) {
            String contentType = fileValidationUtil.validate(file);
            result.add(new FileMetadataResponse(file.getOriginalFilename(), file.getSize(), contentType));
        }
        return result;
    }

    @Override
    @Transactional
    public JobResponse submit(OperationType type, List<MultipartFile> files, Map<String, String> params) {
        requireNonEmpty(files);
        if (!MULTI_FILE.contains(type) && files.size() > 1) {
            throw new FileValidationException(type + " accepts a single file");
        }
        if (type == OperationType.MERGE && files.size() < 2) {
            throw new FileValidationException("Merge requires at least two files");
        }

        // Validate everything up front so a bad file never creates a job.
        List<String> detectedTypes = new ArrayList<>();
        for (MultipartFile file : files) {
            String detected = fileValidationUtil.validate(file);
            String ext = FileValidationUtil.extensionOf(file.getOriginalFilename());
            if (!ALLOWED.get(type).contains(ext)) {
                throw new UnsupportedFileTypeException(
                        "%s does not accept '.%s' files (allowed: %s)".formatted(type, ext, ALLOWED.get(type)));
            }
            detectedTypes.add(detected);
        }

        UUID jobId = UUID.randomUUID();
        ProcessingJob job = ProcessingJob.builder()
                .id(jobId)
                .operationType(type)
                .status(JobStatus.UPLOADED)
                .originalFileName(originalNameSummary(files))
                .parameters(serialize(params))
                .build();
        jobRepository.save(job);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String ext = FileValidationUtil.extensionOf(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            String key = storageService.storeUpload(jobId, file, storedName);
            uploadedFileRepository.save(UploadedFile.builder()
                    .id(UUID.randomUUID())
                    .jobId(jobId)
                    .fileName(file.getOriginalFilename())
                    .filePath(key)
                    .fileSize(file.getSize())
                    .mimeType(detectedTypes.get(i))
                    .sortOrder(i)
                    .build());
        }

        job.setStatus(JobStatus.QUEUED);
        jobRepository.save(job);

        jobQueue.submit(jobId);
        log.info("Queued job {} ({}) with {} input file(s)", jobId, type, files.size());

        return jobMapper.toResponse(job, List.of());
    }

    private void requireNonEmpty(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new FileValidationException("No files supplied");
        }
    }

    private String originalNameSummary(List<MultipartFile> files) {
        if (files.size() == 1) {
            return files.get(0).getOriginalFilename();
        }
        return files.get(0).getOriginalFilename() + " (+" + (files.size() - 1) + " more)";
    }

    private String serialize(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new FileValidationException("Invalid parameters");
        }
    }
}
