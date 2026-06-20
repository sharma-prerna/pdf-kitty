package com.pdftoolkit.processor;

/**
 * Parameter keys passed through {@link ProcessContext#params()}.
 */
public final class ProcessParams {

    public static final String COMPRESSION_LEVEL = "level";
    public static final String SPLIT_MODE = "mode";
    public static final String SPLIT_SPEC = "spec";
    public static final String IMAGE_FORMAT = "format";
    public static final String DPI = "dpi";

    private ProcessParams() {
    }
}
