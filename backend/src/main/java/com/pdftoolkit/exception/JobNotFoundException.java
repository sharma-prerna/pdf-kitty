package com.pdftoolkit.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class JobNotFoundException extends ApiException {

    public JobNotFoundException(UUID id) {
        super(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "Job not found: " + id);
    }
}
