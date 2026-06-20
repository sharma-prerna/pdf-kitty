package com.pdftoolkit.dto;

/**
 * How a PDF should be split.
 * <ul>
 *   <li>{@code RANGE} — a contiguous page range, spec like {@code "1-5"}.</li>
 *   <li>{@code PAGES} — a comma-separated list of pages, spec like {@code "1,3,5"};
 *       each listed page becomes its own document.</li>
 *   <li>{@code EVERY} — every page becomes its own document; spec ignored.</li>
 * </ul>
 */
public enum SplitMode {
    RANGE,
    PAGES,
    EVERY
}
