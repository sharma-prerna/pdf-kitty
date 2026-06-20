package com.pdftoolkit.service;

import org.springframework.core.io.Resource;

import java.util.UUID;

/**
 * Resolves a completed job's output(s) into a single downloadable resource (a file, or a ZIP
 * archive when the job produced multiple outputs).
 */
public interface DownloadService {

    DownloadPayload resolve(UUID jobId);

    record DownloadPayload(Resource resource, String fileName, String contentType, long size) {
    }
}
