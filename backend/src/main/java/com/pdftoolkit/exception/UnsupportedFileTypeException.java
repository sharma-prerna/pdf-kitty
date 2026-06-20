package com.pdftoolkit.exception;

import org.springframework.http.HttpStatus;

public class UnsupportedFileTypeException extends ApiException {

    public UnsupportedFileTypeException(String message) {
        super(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_FILE_TYPE", message);
    }
}
