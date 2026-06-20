package com.pdftoolkit.processor;

import com.pdftoolkit.entity.OperationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link FileProcessor} for an {@link OperationType}. Populated from all
 * processor beans discovered by Spring.
 */
@Component
public class ProcessorRegistry {

    private final Map<OperationType, FileProcessor> processors = new EnumMap<>(OperationType.class);

    public ProcessorRegistry(List<FileProcessor> beans) {
        for (FileProcessor bean : beans) {
            FileProcessor existing = processors.put(bean.type(), bean);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate processor for " + bean.type() + ": "
                                + existing.getClass() + " and " + bean.getClass());
            }
        }
    }

    public FileProcessor get(OperationType type) {
        FileProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalStateException("No processor registered for operation " + type);
        }
        return processor;
    }
}
