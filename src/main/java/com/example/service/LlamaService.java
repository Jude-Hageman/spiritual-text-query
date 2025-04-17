package com.example.service;

import com.example.config.LlamaConfig;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Service class for interacting with the Together.ai Llama API.
 */
@Service
@RequiredArgsConstructor
public class LlamaService {

    private final LlamaConfig llamaConfig;
    private final RestTemplate restTemplate;

    /**
     * Processes text using the Together.ai Llama API.
     *
     * @param prompt The input text to process
     * @return The API response as a String
     */
    public String processText(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + llamaConfig.getApiKey());

        Map<String, Object> requestBody = Map.of(
            "model", "togethercomputer/llama-2-70b-chat",
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            ),
            "temperature", 0.7,
            "max_tokens", 500
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            llamaConfig.getApiUrl(),
            HttpMethod.POST,
            request,
            String.class
        );

        return response.getBody();
    }
}
