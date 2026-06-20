package com.pdftoolkit.config;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires a headless LibreOffice office manager and a {@link DocumentConverter} for Word -> PDF
 * conversion. Active only when {@code jodconverter.local.enabled=true} (set in the docker
 * profile, where LibreOffice is installed); disabled by default so the app boots without
 * LibreOffice in local dev and tests.
 */
@Configuration
@ConditionalOnProperty(prefix = "jodconverter.local", name = "enabled", havingValue = "true")
public class JodConverterConfig {

    @Value("${jodconverter.local.office-home:}")
    private String officeHome;

    @Value("${jodconverter.local.max-tasks-per-process:50}")
    private int maxTasksPerProcess;

    @Value("${jodconverter.local.process-timeout:120000}")
    private long processTimeout;

    @Value("${jodconverter.local.task-execution-timeout:120000}")
    private long taskExecutionTimeout;

    /** Starts/stops the LibreOffice process pool with the application context. */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager officeManager() {
        LocalOfficeManager.Builder builder = LocalOfficeManager.builder()
                .maxTasksPerProcess(maxTasksPerProcess)
                .processTimeout(processTimeout)
                .taskExecutionTimeout(taskExecutionTimeout);
        if (officeHome != null && !officeHome.isBlank()) {
            builder.officeHome(officeHome);
        }
        return builder.build();
    }

    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }
}
