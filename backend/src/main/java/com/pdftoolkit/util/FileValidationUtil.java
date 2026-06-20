package com.pdftoolkit.util;

import com.pdftoolkit.config.AppProperties;
import com.pdftoolkit.exception.FileValidationException;
import com.pdftoolkit.exception.UnsupportedFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

/**
 * Validates uploads by size, extension, and Tika-detected content type (magic bytes), so a
 * renamed executable cannot pass as a PDF.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationUtil {

    private final AppProperties properties;
    private final Tika tika = new Tika();

    /**
     * Validate a single file. Returns the detected MIME type on success.
     *
     * @throws FileValidationException        if empty or too large
     * @throws UnsupportedFileTypeException   if the extension or detected type is not allowed
     */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("Uploaded file is empty");
        }

        long maxBytes = properties.getFile().getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new FileValidationException(
                    "File '%s' exceeds the %d MB limit".formatted(
                            file.getOriginalFilename(), properties.getFile().getMaxSizeMb()));
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!properties.getFile().getAllowedExtensions().contains(extension)) {
            throw new UnsupportedFileTypeException(
                    "Unsupported file extension '%s'. Allowed: %s".formatted(
                            extension, properties.getFile().getAllowedExtensions()));
        }

        String detected;
        try {
            detected = tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new FileValidationException("Could not read uploaded file: " + file.getOriginalFilename());
        }

        if (!properties.getFile().getAllowedMimeTypes().contains(detected)) {
            throw new UnsupportedFileTypeException(
                    "File content type '%s' is not allowed".formatted(detected));
        }
        return detected;
    }

    /** Validate that a file's extension is one of the expected set (e.g. only "pdf"). */
    public void requireExtension(MultipartFile file, String... expected) {
        String ext = extensionOf(file.getOriginalFilename());
        for (String e : expected) {
            if (e.equalsIgnoreCase(ext)) {
                return;
            }
        }
        throw new UnsupportedFileTypeException(
                "File '%s' must be one of %s".formatted(file.getOriginalFilename(), String.join(", ", expected)));
    }

    public static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
