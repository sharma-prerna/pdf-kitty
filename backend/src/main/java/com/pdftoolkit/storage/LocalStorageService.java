package com.pdftoolkit.storage;

import com.pdftoolkit.config.AppProperties;
import com.pdftoolkit.exception.StorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Local-filesystem {@link StorageService}. Files live under
 * {@code <baseDir>/<jobId>/<fileName>}. Every resolved path is verified to stay within the
 * configured base directory to defend against path traversal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final AppProperties properties;
    private Path baseDir;

    @PostConstruct
    void init() {
        this.baseDir = Path.of(properties.getStorage().getBaseDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
            log.info("Local storage base directory: {}", baseDir);
        } catch (IOException e) {
            throw new StorageException("Could not create storage base directory: " + baseDir, e);
        }
    }

    @Override
    public String storeUpload(UUID jobId, MultipartFile file, String storedFileName) {
        try (InputStream in = file.getInputStream()) {
            return writeStream(jobId, in, storedFileName);
        } catch (IOException e) {
            throw new StorageException("Failed to store upload for job " + jobId, e);
        }
    }

    @Override
    public String storeOutput(UUID jobId, byte[] content, String fileName) {
        Path target = secureResolve(jobId.toString(), fileName);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return relativeKey(target);
        } catch (IOException e) {
            throw new StorageException("Failed to store output for job " + jobId, e);
        }
    }

    @Override
    public String storeOutput(UUID jobId, InputStream content, String fileName) {
        try {
            return writeStream(jobId, content, fileName);
        } catch (IOException e) {
            throw new StorageException("Failed to store output for job " + jobId, e);
        }
    }

    private String writeStream(UUID jobId, InputStream in, String fileName) throws IOException {
        Path target = secureResolve(jobId.toString(), fileName);
        Files.createDirectories(target.getParent());
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        return relativeKey(target);
    }

    @Override
    public Path resolve(String storageKey) {
        Path resolved = baseDir.resolve(storageKey).normalize();
        assertWithinBase(resolved);
        return resolved;
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        Path path = resolve(storageKey);
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new StorageException("File not found or not readable: " + storageKey);
        }
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new StorageException("Could not read file: " + storageKey, e);
        }
    }

    @Override
    public void deleteJob(UUID jobId) {
        Path jobDir = secureResolve(jobId.toString(), null);
        if (!Files.exists(jobDir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(jobDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("Failed to delete {}: {}", p, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Failed to delete job directory {}: {}", jobDir, e.getMessage());
        }
    }

    /**
     * Resolve {@code <baseDir>/<jobSegment>/<fileName>} and reject anything that escapes the
     * base directory. The file name is reduced to its last path element to neutralise
     * traversal sequences supplied by clients.
     */
    private Path secureResolve(String jobSegment, String fileName) {
        Path dir = baseDir.resolve(jobSegment).normalize();
        assertWithinBase(dir);
        if (fileName == null) {
            return dir;
        }
        String safeName = Path.of(fileName).getFileName().toString();
        Path resolved = dir.resolve(safeName).normalize();
        assertWithinBase(resolved);
        return resolved;
    }

    private void assertWithinBase(Path path) {
        if (!path.startsWith(baseDir)) {
            throw new StorageException("Path traversal detected: " + path);
        }
    }

    private String relativeKey(Path target) {
        return baseDir.relativize(target).toString().replace('\\', '/');
    }
}
