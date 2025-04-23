package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Service
@Slf4j
public class ESVService {
    
    @Value("${esv.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private static final String ESV_API_BASE_URL = "https://api.esv.org/v3/passage/text/";
    
    public ESVService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public String getVerseText(String reference) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + apiKey);
        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(ESV_API_BASE_URL)
            .queryParam("q", reference)
            .queryParam("include-headings", false)
            .queryParam("include-footnotes", false)
            .queryParam("include-verse-numbers", false)
            .queryParam("include-short-copyright", false)
            .queryParam("include-passage-references", false);
            
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
            );
            
            JSONObject jsonResponse = new JSONObject(response.getBody());
            String passages = jsonResponse.getJSONArray("passages").getString(0);
            return passages.trim();
            
        } catch (Exception e) {
            log.error("Error fetching verse from ESV API: {}", e.getMessage());
            throw e;
        }
    }
}
