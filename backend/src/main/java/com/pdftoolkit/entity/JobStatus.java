package com.pdftoolkit.entity;

/**
 * Lifecycle states for a {@link ProcessingJob}.
 */
public enum JobStatus {
    UPLOADED,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
