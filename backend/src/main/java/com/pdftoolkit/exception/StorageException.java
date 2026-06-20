package com.pdftoolkit.exception;

import org.springframework.http.HttpStatus;

/** Raised on storage failures, including path-traversal attempts. */
public class StorageException extends ApiException {

    public StorageException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message);
    }

    public StorageException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message, cause);
    }
}
