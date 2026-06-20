package com.pdftoolkit.processor;

import java.util.List;

/**
 * Result of a processor run: one or more generated outputs.
 */
public record ProcessResult(List<ProcessOutput> outputs) {

    public ProcessResult {
        outputs = List.copyOf(outputs);
    }

    public static ProcessResult single(ProcessOutput output) {
        return new ProcessResult(List.of(output));
    }

    public boolean isMulti() {
        return outputs.size() > 1;
    }
}
