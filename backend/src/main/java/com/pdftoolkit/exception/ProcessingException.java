package com.pdftoolkit.exception;

import org.springframework.http.HttpStatus;

/** Raised when a document processor fails to produce its output. */
public class ProcessingException extends ApiException {

    public ProcessingException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "PROCESSING_FAILED", message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "PROCESSING_FAILED", message, cause);
    }
}
