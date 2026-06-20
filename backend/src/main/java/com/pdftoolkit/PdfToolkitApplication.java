package com.pdftoolkit;

import com.pdftoolkit.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableConfigurationProperties(AppProperties.class)
public class PdfToolkitApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdfToolkitApplication.class, args);
    }
}
