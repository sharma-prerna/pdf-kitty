package com.pdftoolkit.processor.impl;

import com.pdftoolkit.dto.SplitMode;
import com.pdftoolkit.entity.OperationType;
import com.pdftoolkit.exception.ProcessingException;
import com.pdftoolkit.processor.FileProcessor;
import com.pdftoolkit.processor.ProcessContext;
import com.pdftoolkit.processor.ProcessInput;
import com.pdftoolkit.processor.ProcessOutput;
import com.pdftoolkit.processor.ProcessParams;
import com.pdftoolkit.processor.ProcessResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Splits a PDF by contiguous range, an explicit page list, or one document per page.
 *
 * <ul>
 *   <li>{@link SplitMode#RANGE} ({@code spec="1-5"}) — one PDF containing that range.</li>
 *   <li>{@link SplitMode#PAGES} ({@code spec="1,3,5"}) — one PDF per listed page.</li>
 *   <li>{@link SplitMode#EVERY} — one PDF per page.</li>
 * </ul>
 */
@Component
public class SplitProcessor implements FileProcessor {

    @Override
    public OperationType type() {
        return OperationType.SPLIT;
    }

    @Override
    public ProcessResult process(ProcessContext context) throws Exception {
        ProcessInput input = context.firstInput();
        SplitMode mode = SplitMode.valueOf(
                context.param(ProcessParams.SPLIT_MODE).orElse("EVERY").toUpperCase(Locale.ROOT));
        String spec = context.param(ProcessParams.SPLIT_SPEC).orElse("");

        try (PDDocument document = Loader.loadPDF(input.path().toFile())) {
            int pageCount = document.getNumberOfPages();
            List<ProcessOutput> outputs = switch (mode) {
                case RANGE -> List.of(extractPages(document, parseRange(spec, pageCount), "pages-" + spec + ".pdf"));
                case PAGES -> singlePagePerEntry(document, parseList(spec, pageCount));
                case EVERY -> singlePagePerEntry(document, allPages(pageCount));
            };
            if (outputs.isEmpty()) {
                throw new ProcessingException("Split produced no pages for spec '" + spec + "'");
            }
            return new ProcessResult(outputs);
        }
    }

    private List<ProcessOutput> singlePagePerEntry(PDDocument document, List<Integer> pages) throws Exception {
        List<ProcessOutput> outputs = new ArrayList<>();
        for (int pageNumber : pages) {
            outputs.add(extractPages(document, List.of(pageNumber), "page-" + pageNumber + ".pdf"));
        }
        return outputs;
    }

    /** Build a new PDF from the given 1-based page numbers. */
    private ProcessOutput extractPages(PDDocument source, List<Integer> pages, String fileName) throws Exception {
        try (PDDocument target = new PDDocument()) {
            for (int pageNumber : pages) {
                PDPage imported = target.importPage(source.getPage(pageNumber - 1));
                imported.setRotation(source.getPage(pageNumber - 1).getRotation());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            target.save(out);
            return new ProcessOutput(fileName, "application/pdf", out.toByteArray());
        }
    }

    private List<Integer> allPages(int pageCount) {
        List<Integer> pages = new ArrayList<>();
        for (int i = 1; i <= pageCount; i++) {
            pages.add(i);
        }
        return pages;
    }

    private List<Integer> parseRange(String spec, int pageCount) {
        String[] parts = spec.split("-");
        if (parts.length != 2) {
            throw new ProcessingException("Range spec must be 'start-end', got: " + spec);
        }
        int start = parsePage(parts[0].trim(), pageCount);
        int end = parsePage(parts[1].trim(), pageCount);
        if (start > end) {
            throw new ProcessingException("Range start must not exceed end: " + spec);
        }
        List<Integer> pages = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        return pages;
    }

    private List<Integer> parseList(String spec, int pageCount) {
        Set<Integer> pages = new LinkedHashSet<>();
        for (String token : spec.split(",")) {
            if (!token.isBlank()) {
                pages.add(parsePage(token.trim(), pageCount));
            }
        }
        if (pages.isEmpty()) {
            throw new ProcessingException("Page list is empty: " + spec);
        }
        return new ArrayList<>(pages);
    }

    private int parsePage(String token, int pageCount) {
        int page;
        try {
            page = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new ProcessingException("Invalid page number: " + token);
        }
        if (page < 1 || page > pageCount) {
            throw new ProcessingException("Page " + page + " out of bounds (1.." + pageCount + ")");
        }
        return page;
    }
}
