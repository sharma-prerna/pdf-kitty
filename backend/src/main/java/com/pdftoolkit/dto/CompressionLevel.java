package com.pdftoolkit.dto;

/**
 * Compression strength. Each level maps to a target image DPI and JPEG quality used when
 * re-encoding embedded raster images.
 */
public enum CompressionLevel {
    LOW(150, 0.8f),
    MEDIUM(120, 0.6f),
    HIGH(72, 0.4f);

    private final int targetDpi;
    private final float jpegQuality;

    CompressionLevel(int targetDpi, float jpegQuality) {
        this.targetDpi = targetDpi;
        this.jpegQuality = jpegQuality;
    }

    public int targetDpi() {
        return targetDpi;
    }

    public float jpegQuality() {
        return jpegQuality;
    }
}
