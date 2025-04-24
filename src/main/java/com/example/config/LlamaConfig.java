package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class for Llama API settings.
 */
@Configuration
@Getter
@Slf4j
public class LlamaConfig {
    
    private final Environment environment;
    
    @Value("${llama.api.key}")
    private String apiKey;
    
    @Value("${llama.api.url}")
    private String apiUrl;
    
    public LlamaConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * Initializes the LlamaConfig class.
     */
    @PostConstruct
    public void init() {
        // Log configuration details
        log.info("Active profiles: {}", String.join(", ", environment.getActiveProfiles()));
        log.info("Default profiles: {}", String.join(", ", environment.getDefaultProfiles()));
        log.info("Llama API URL configured as: {}", apiUrl);
        log.info("Llama API Key present: {}", apiKey != null && !apiKey.isBlank());
        log.info("Llama API Key value: {}", apiKey); // Be careful with this in production!
        
        // Print all property sources
        if (environment instanceof org.springframework.core.env.ConfigurableEnvironment) {
            org.springframework.core.env.ConfigurableEnvironment configurableEnvironment = (org.springframework.core.env.ConfigurableEnvironment) environment;
            log.info("Property Sources:");
            for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
                log.info("  - {}", propertySource.getName());
                if (propertySource.containsProperty("llama.api.key")) {
                    log.info("    Contains llama.api.key");
                }
            }
        }
    }
}
