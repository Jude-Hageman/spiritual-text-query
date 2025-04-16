package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * REST Controller for handling Bible search requests.
 * Provides endpoints to search the ESV Bible API.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class BibleSearchController {

    @Value("${esv.api.key}")
    private String esvApiKey;


    /**
     * Searches the ESV Bible API with the provided query.
     *
     * @param q The search query string
     * @return ResponseEntity containing the search results from ESV API
     */
    @GetMapping("/search")
    public ResponseEntity<String> searchBible(@RequestParam String q) {
        String url = "https://api.esv.org/v3/passage/search/?q=" + q;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + esvApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return ResponseEntity.ok(response.getBody());
    }
}
