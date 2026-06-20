package com.pdftoolkit.exception;

import org.springframework.http.HttpStatus;

/** Raised when an upload violates a validation rule (size, empty, count, malformed param). */
public class FileValidationException extends ApiException {

    public FileValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, "FILE_VALIDATION_FAILED", message);
    }
}
