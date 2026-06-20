package com.pdftoolkit.processor;

import com.pdftoolkit.entity.OperationType;

/**
 * Strategy for a single document operation. Implementations are stateless Spring beans
 * registered in the {@link ProcessorRegistry} by their {@link #type()}.
 */
public interface FileProcessor {

    /** The operation this processor handles. */
    OperationType type();

    /** Run the operation. Implementations must not mutate the input files. */
    ProcessResult process(ProcessContext context) throws Exception;
}
