package com.pdftoolkit.processor;

import java.nio.file.Path;

/**
 * One resolved input file for a processor.
 *
 * @param fileName original client file name (for output naming/logging only)
 * @param path     absolute path on the backing store
 * @param mimeType detected content type
 */
public record ProcessInput(String fileName, Path path, String mimeType) {
}
