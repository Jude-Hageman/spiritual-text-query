package com.example.controller;

import com.example.service.LlamaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Controller for handling Llama API requests.
 */
@RestController
@RequestMapping("/api/llama")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class LlamaController {

    private final LlamaService llamaService;

    /**
     * Processes text using the Llama API.
     *
     * @param prompt The input text to process
     * @return The processed text from Llama API
     */
    @PostMapping("/process")
    public ResponseEntity<String> processText(@RequestBody String prompt) {
        if (!StringUtils.hasText(prompt)) {
            return ResponseEntity.badRequest().body("Prompt cannot be empty");
        }
        String response = llamaService.processText(prompt);
        return ResponseEntity.ok(response);
    }
}
