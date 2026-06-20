package com.pdftoolkit.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Abstraction over the file store. The local implementation writes to a base directory;
 * a future {@code S3StorageService} can implement the same contract.
 *
 * <p>All files are namespaced under a job id and stored with server-generated names; the
 * original client filename is never used to construct a path (path-traversal protection).
 */
public interface StorageService {

    /** Store an uploaded multipart file under the job, returning the relative storage key. */
    String storeUpload(UUID jobId, MultipartFile file, String storedFileName);

    /** Store generated output bytes under the job, returning the relative storage key. */
    String storeOutput(UUID jobId, byte[] content, String fileName);

    /** Store generated output from a stream under the job, returning the relative storage key. */
    String storeOutput(UUID jobId, InputStream content, String fileName);

    /** Resolve a stored key to an absolute path on the backing store. */
    Path resolve(String storageKey);

    /** Load a stored key as a readable Spring resource. */
    Resource loadAsResource(String storageKey);

    /** Recursively delete all files for a job. */
    void deleteJob(UUID jobId);
}
