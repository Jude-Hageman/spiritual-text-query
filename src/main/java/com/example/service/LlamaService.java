package com.example.service;

import com.example.config.LlamaConfig;
import com.example.util.BibleReferenceParser;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

/**
 * Service class for interacting with the Together.ai Llama API.
 */
@Service
@Slf4j
public class LlamaService extends TextProcessingService {

    private final LlamaConfig llamaConfig;
    private final ESVService esvService;
    private final BibleReferenceParser bibleReferenceParser;

    public LlamaService(RestTemplate restTemplate, LlamaConfig llamaConfig, 
                       ESVService esvService, BibleReferenceParser bibleReferenceParser) {
        super(restTemplate);
        this.llamaConfig = llamaConfig;
        this.esvService = esvService;
        this.bibleReferenceParser = bibleReferenceParser;
    }

    @Override
    public String processText(String prompt) throws Exception {
        try {
            // Check if the prompt contains a Bible reference
            if (bibleReferenceParser.containsBibleReference(prompt)) {
                String reference = bibleReferenceParser.extractReference(prompt);
                if (reference != null) {
                    // Get the verse text from ESV API
                    String verseText = esvService.getVerseText(reference);
                    // Append verse text to prompt
                    prompt = prompt + "\n\nBible verse text: " + verseText;
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String apiKey = llamaConfig.getApiKey();
            String authHeader = "Bearer " + apiKey;
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

            log.debug("Making request to Llama API at URL: {}", llamaConfig.getApiUrl());
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                llamaConfig.getApiUrl(),
                HttpMethod.POST,
                request,
                String.class
            );
            
            String responseBody = response.getBody();
            JSONObject jsonResponse = new JSONObject(responseBody);
            String content = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
            
            return content.trim();
        } catch (Exception e) {
            handleApiError(e, "Llama");
            throw e;
        }
    }
}
