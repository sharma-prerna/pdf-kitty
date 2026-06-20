package com.pdftoolkit.queue;

import java.util.UUID;

/**
 * Seam between request handling and background processing. The current implementation
 * dispatches to an in-process async executor; it can be replaced by a Redis Streams / broker
 * backed implementation without changing callers.
 */
public interface JobQueue {

    /** Hand a persisted, QUEUED job off for asynchronous processing. */
    void submit(UUID jobId);
}
