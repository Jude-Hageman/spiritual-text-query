package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Configuration class for Llama API settings.
 */
@Configuration
@Getter
public class LlamaConfig {
    
    @Value("${llama.api.key}")
    private String apiKey;
    
    @Value("${llama.api.url:https://api.meta.com/llama/v4/scout}")
    private String apiUrl;
}
