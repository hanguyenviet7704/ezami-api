package com.hth.udecareer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuration for serving static uploaded files.
 * Allows access to files in uploads/ directory via /uploads/** URL pattern.
 */
@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path to uploads directory
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();

        log.info("Configuring static resource handler for uploads directory: {}", uploadPath);

        // Serve files from uploads/ directory at /uploads/** URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(31536000); // 1 year cache

        log.info("Static files from {} will be accessible at /uploads/**", uploadPath);
    }
}
