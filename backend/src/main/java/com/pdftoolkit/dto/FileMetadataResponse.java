package com.pdftoolkit.dto;

/**
 * Validation result for a single uploaded file (returned by the standalone upload endpoint).
 */
public record FileMetadataResponse(
        String fileName,
        long size,
        String contentType
) {
}
