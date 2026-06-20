package com.pdftoolkit.dto;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error payload returned by {@code GlobalExceptionHandler}.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<String> details
) {
    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(Instant.now(), status, code, message, path, List.of());
    }

    public static ErrorResponse of(int status, String code, String message, String path, List<String> details) {
        return new ErrorResponse(Instant.now(), status, code, message, path, details);
    }
}
