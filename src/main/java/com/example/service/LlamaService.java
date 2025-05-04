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
    
    private static final String BIBLE_CONTEXT_PROMPT = "You are a biblical assistant. Only answer questions related to the Bible, biblical interpretation, theology, and religious matters. If a question is not related to these topics, politely inform the user that you can only assist with Bible-related questions. ";
    
    private static final String NON_BIBLICAL_RESPONSE = "I apologize, but I can only assist with questions related to the Bible, biblical interpretation, theology, and religious matters. Please rephrase your question to focus on these topics.";

    public LlamaService(RestTemplate restTemplate, LlamaConfig llamaConfig, 
                       ESVService esvService, BibleReferenceParser bibleReferenceParser) {
        super(restTemplate);
        this.llamaConfig = llamaConfig;
        this.esvService = esvService;
        this.bibleReferenceParser = bibleReferenceParser;
    }

    /**
     * Determines if a question is related to biblical or religious topics.
     *
     * @param text The text to analyze
     * @return true if the text contains biblical keywords or references
     */
    protected boolean isBibleRelatedQuestion(String text) {
        // List of keywords related to biblical/religious content
        String[] biblicalKeywords = {
            "bible", "scripture", "jesus", "god", "christ", "holy spirit",
            "psalm", "gospel", "prophet", "apostle", "church", "faith",
            "prayer", "worship", "sin", "salvation", "heaven", "hell",
            "christian", "biblical", "testament", "verse", "chapter",
            "moses", "abraham", "david", "paul", "peter", "mary",
            "cross", "resurrection", "baptism", "communion", "trinity",
            "sermon", "pastor", "priest", "minister", "theology",
            "spiritual", "religious", "sacred", "divine", "lord"
        };
        
        String lowerText = text.toLowerCase();
        // Check if the text contains any biblical keywords
        for (String keyword : biblicalKeywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        // Also return true if it contains a Bible reference
        return bibleReferenceParser.containsBibleReference(text);
    }

    @Override
    public String processText(String prompt) throws Exception {
        try {
            // First check if the question is Bible-related
            if (!isBibleRelatedQuestion(prompt)) {
                return NON_BIBLICAL_RESPONSE;
            }

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

            // Add the Bible context to the prompt
            prompt = BIBLE_CONTEXT_PROMPT + "\n\nUser question: " + prompt;

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
