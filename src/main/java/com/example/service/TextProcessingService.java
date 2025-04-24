package com.example.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TextProcessingService {
    protected final RestTemplate restTemplate;

    protected TextProcessingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Process the input text according to the specific service implementation.
     *
     * @param input The text to process
     * @return The processed text
     * @throws Exception if processing fails
     */
    public abstract String processText(String input) throws Exception;

    /**
     * Handles API errors in a consistent way across services.
     *
     * @param e The exception that occurred
     * @param serviceName The name of the service where the error occurred
     * @throws Exception the handled exception
     */
    protected void handleApiError(Exception e, String serviceName) throws Exception {
        log.error("Error in {} service: {}", serviceName, e.getMessage());
        throw e;
    }
}
