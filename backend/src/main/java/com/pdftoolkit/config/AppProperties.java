package com.pdftoolkit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * Strongly-typed application configuration bound from the {@code app.*} namespace.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NestedConfigurationProperty
    private File file = new File();

    @NestedConfigurationProperty
    private Storage storage = new Storage();

    @NestedConfigurationProperty
    private RateLimit rateLimit = new RateLimit();

    @NestedConfigurationProperty
    private Security security = new Security();

    @NestedConfigurationProperty
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class File {
        /** Maximum accepted size per uploaded file, in megabytes. */
        private long maxSizeMb = 50;
        /** Allowed lower-case file extensions. */
        private List<String> allowedExtensions = List.of("pdf", "jpg", "jpeg", "png");
        /** Allowed MIME types (validated against Tika-detected content). */
        private List<String> allowedMimeTypes = List.of(
                "application/pdf",
                "application/zip",
                "image/jpeg",
                "image/png");
    }

    @Getter
    @Setter
    public static class Storage {
        /** Root directory for uploaded and generated files. */
        private String baseDir = "./data/storage";
    }

    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = true;
        /** Allowed requests per window per client IP. */
        private int requests = 60;
        /** Window length in seconds. */
        private int windowSeconds = 60;
    }

    @Getter
    @Setter
    public static class Security {
        /** HMAC signing secret (>= 32 chars). Used by JwtService. */
        private String jwtSecret = "change-me-please-change-me-please-change-me-32+";
        /** Token validity in milliseconds. */
        private long jwtExpirationMs = 3_600_000;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");
    }
}
