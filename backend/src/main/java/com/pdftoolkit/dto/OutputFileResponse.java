package com.pdftoolkit.dto;

import java.util.UUID;

/**
 * Metadata for a single generated output file.
 */
public record OutputFileResponse(
        UUID id,
        String fileName,
        long fileSize,
        String mimeType
) {
}
