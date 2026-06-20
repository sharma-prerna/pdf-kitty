package com.pdftoolkit.dto;

import com.pdftoolkit.entity.JobStatus;
import com.pdftoolkit.entity.OperationType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Public view of a {@link com.pdftoolkit.entity.ProcessingJob}, returned by operation,
 * status, and history endpoints.
 */
public record JobResponse(
        UUID id,
        OperationType operationType,
        JobStatus status,
        String originalFileName,
        String outputFileName,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        Instant processingStartedAt,
        Instant completedAt,
        List<OutputFileResponse> outputs
) {
}
