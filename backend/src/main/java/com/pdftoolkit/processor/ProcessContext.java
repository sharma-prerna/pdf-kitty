package com.pdftoolkit.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable input bundle handed to a {@link FileProcessor}.
 */
public record ProcessContext(UUID jobId, List<ProcessInput> inputs, Map<String, String> params) {

    public ProcessContext {
        inputs = List.copyOf(inputs);
        params = params == null ? Map.of() : Map.copyOf(params);
    }

    public ProcessInput firstInput() {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("No input files supplied");
        }
        return inputs.get(0);
    }

    public Optional<String> param(String key) {
        return Optional.ofNullable(params.get(key));
    }

    public String requireParam(String key) {
        return param(key).orElseThrow(
                () -> new IllegalArgumentException("Missing required parameter: " + key));
    }

    public int intParam(String key, int defaultValue) {
        return param(key).map(Integer::parseInt).orElse(defaultValue);
    }
}
