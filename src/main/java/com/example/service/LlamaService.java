package com.example.service;

import com.example.config.LlamaConfig;
import com.example.util.BibleReferenceParser;
import com.example.service.ESVService;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

/**
 * Service class for interacting with the Together.ai Llama API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlamaService {

    private final LlamaConfig llamaConfig;
    private final RestTemplate restTemplate;
    private final ESVService esvService;
    private final BibleReferenceParser bibleReferenceParser;

    /**
     * Processes text using the Together.ai Llama API.
     *
     * @param prompt The input text to process
     * @return The API response as a String
     */
    public String processText(String prompt) {
        // Check if the prompt contains a Bible reference
        if (bibleReferenceParser.containsBibleReference(prompt)) {
            String reference = bibleReferenceParser.extractReference(prompt);
            if (reference != null) {
                try {
                    // Get the verse text from ESV API
                    String verseText = esvService.getVerseText(reference);
                    
                    // Create an enhanced prompt with the verse text
                    prompt = String.format("Explain the meaning of the Bible verse %s: '%s'", 
                        reference, verseText);
                    log.info("Enhanced prompt with verse text: {}", prompt);
                } catch (Exception e) {
                    log.warn("Failed to fetch verse text, proceeding with original prompt: {}", e.getMessage());
                }
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String apiKey = llamaConfig.getApiKey();
        log.info("API URL: {}", llamaConfig.getApiUrl());
        log.info("API Key length: {}", apiKey.length());
        String authHeader = "Bearer " + apiKey;
        log.info("Authorization header: {}", authHeader);
        headers.set("Authorization", authHeader);

        Map<String, Object> requestBody = Map.of(
            "model", "mistralai/Mistral-7B-Instruct-v0.1",
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            ),
            "temperature", 0.7,
            "max_tokens", 500
        );

        log.info("Request body: {}", new JSONObject(requestBody).toString(2));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                llamaConfig.getApiUrl(),
                HttpMethod.POST,
                request,
                String.class
            );
            String responseBody = response.getBody();
            log.info("API Response: {}", responseBody);
            
            // Extract the actual message content from the response
            JSONObject jsonResponse = new JSONObject(responseBody);
            String content = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
            
            return content.trim();
        } catch (Exception e) {
            log.error("Error calling Llama API: {}", e.getMessage());
            throw e;
        }
    }
}
