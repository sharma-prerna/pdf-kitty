package com.pdftoolkit.util;

import com.pdftoolkit.exception.StorageException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bundles multiple files into a single ZIP archive.
 */
public final class ZipUtil {

    private ZipUtil() {
    }

    /** A file to include in an archive, with the name it should have inside the ZIP. */
    public record Entry(String name, Path path) {
    }

    /** Stream the given entries into {@code out} as a ZIP archive. */
    public static void zip(List<Entry> entries, OutputStream out) {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (Entry entry : entries) {
                zos.putNextEntry(new ZipEntry(entry.name()));
                Files.copy(entry.path(), zos);
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new StorageException("Failed to build ZIP archive", e);
        }
    }
}
