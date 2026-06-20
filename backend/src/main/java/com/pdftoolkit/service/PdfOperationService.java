package com.pdftoolkit.service;

import com.pdftoolkit.dto.FileMetadataResponse;
import com.pdftoolkit.dto.JobResponse;
import com.pdftoolkit.entity.OperationType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Write-side: validates uploads, creates a job, and enqueues it for processing.
 */
public interface PdfOperationService {

    /** Validate the given files without creating a job. */
    List<FileMetadataResponse> validateOnly(List<MultipartFile> files);

    /** Create and enqueue a job for the given operation. Returns the job in QUEUED state. */
    JobResponse submit(OperationType type, List<MultipartFile> files, Map<String, String> params);
}
