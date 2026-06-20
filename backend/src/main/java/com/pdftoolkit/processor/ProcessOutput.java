package com.pdftoolkit.processor;

/**
 * One generated output file, held in memory until persisted by the job runner.
 *
 * @param fileName suggested output file name
 * @param mimeType output content type
 * @param content  output bytes
 */
public record ProcessOutput(String fileName, String mimeType, byte[] content) {
}
