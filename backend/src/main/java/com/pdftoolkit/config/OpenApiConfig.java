package com.pdftoolkit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pdfToolkitOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PDF Toolkit API")
                .description("Upload, process, convert, and download PDF and document files.")
                .version("1.0.0")
                .license(new License().name("MIT")));
    }
}
