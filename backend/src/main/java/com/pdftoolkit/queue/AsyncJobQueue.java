package com.pdftoolkit.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Default {@link JobQueue} that hands jobs to the {@link JobRunner}, whose {@code run} method
 * is {@code @Async} and therefore returns control to the caller immediately.
 *
 * <p>When called within an active transaction, dispatch is deferred until <em>after commit</em>
 * so the async worker is guaranteed to see the persisted job and its uploaded files (otherwise
 * the worker could start before the creating transaction commits and find nothing).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncJobQueue implements JobQueue {

    private final JobRunner jobRunner;

    @Override
    public void submit(UUID jobId) {
        log.debug("Submitting job {} to async runner", jobId);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    jobRunner.run(jobId);
                }
            });
        } else {
            jobRunner.run(jobId);
        }
    }
}
