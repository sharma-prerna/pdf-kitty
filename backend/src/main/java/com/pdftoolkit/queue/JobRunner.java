package com.pdftoolkit.queue;

import java.util.UUID;

/**
 * Executes the actual work for a job. Implemented in the service layer (transitions the job
 * through PROCESSING → COMPLETED/FAILED). Kept as an interface so the queue package does not
 * depend on service internals.
 */
public interface JobRunner {

    /** Process the job. Implementations run asynchronously. */
    void run(UUID jobId);
}
