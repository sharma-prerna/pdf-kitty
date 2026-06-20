package com.pdftoolkit.dto;

/**
 * Output raster image format for the PDF-to-image operation.
 */
public enum ImageFormat {
    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png");

    private final String extension;
    private final String mimeType;

    ImageFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String extension() {
        return extension;
    }

    public String mimeType() {
        return mimeType;
    }
}
