package com.example.service;

import com.example.config.LlamaConfig;
import com.example.util.BibleReferenceParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LlamaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LlamaConfig llamaConfig;

    @Mock
    private ESVService esvService;

    @Mock
    private BibleReferenceParser bibleReferenceParser;

    private LlamaService llamaService;

    @BeforeEach
    void setUp() {
        llamaService = new LlamaService(restTemplate, llamaConfig, esvService, bibleReferenceParser);
    }

    @Test
    void shouldIdentifyBibleRelatedQuestion() {
        // Test with biblical keywords
        when(bibleReferenceParser.containsBibleReference(any())).thenReturn(false);
        assertTrue(llamaService.isBibleRelatedQuestion("What does the Bible say about love?"));
        assertTrue(llamaService.isBibleRelatedQuestion("Tell me about Jesus Christ"));
        assertTrue(llamaService.isBibleRelatedQuestion("Explain the concept of salvation"));
        
        // Test with Bible references
        when(bibleReferenceParser.containsBibleReference("John 3:16")).thenReturn(true);
        assertTrue(llamaService.isBibleRelatedQuestion("John 3:16"));
    }

    @Test
    void shouldIdentifyNonBibleRelatedQuestion() {
        when(bibleReferenceParser.containsBibleReference(any())).thenReturn(false);
        
        assertFalse(llamaService.isBibleRelatedQuestion("What is the weather today?"));
        assertFalse(llamaService.isBibleRelatedQuestion("How do I cook pasta?"));
        assertFalse(llamaService.isBibleRelatedQuestion("Tell me about quantum physics"));
    }

    @Test
    void shouldReturnNonBiblicalResponseForNonBiblicalQuestions() throws Exception {
        when(bibleReferenceParser.containsBibleReference(any())).thenReturn(false);
        
        String response = llamaService.processText("What is the weather like today?");
        assertTrue(response.contains("I apologize"));
        assertTrue(response.contains("only assist with questions related to the Bible"));
    }

    @Test
    void shouldProcessBibleRelatedQuestion() throws Exception {
        // Setup
        String question = "What does the Bible say about love?";
        String apiResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "The Bible teaches that love is central to Christian faith."
                    }
                }]
            }
            """;

        // Mock dependencies
        when(bibleReferenceParser.containsBibleReference(any())).thenReturn(false);
        when(llamaConfig.getApiKey()).thenReturn("test-api-key");
        when(llamaConfig.getApiUrl()).thenReturn("https://api.test.com");

        // Mock API response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("https://api.test.com"),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        )).thenReturn(responseEntity);

        // Execute
        String response = llamaService.processText(question);

        // Verify
        assertNotNull(response);
        assertTrue(response.contains("Bible teaches that love"));
    }

    @Test
    void shouldIncludeBibleVerseInPrompt() throws Exception {
        // Setup
        String question = "explain john 3:16";
        String verseText = "For God so loved the world...";
        String apiResponse = """
            {
                "choices": [{
                    "message": {
                        "content": "This verse teaches about God's love."
                    }
                }]
            }
            """;

        // Mock dependencies
        when(bibleReferenceParser.containsBibleReference(any())).thenReturn(true);
        when(bibleReferenceParser.extractReference(any())).thenReturn("John 3:16");
        when(esvService.getVerseText(any())).thenReturn(verseText);
        when(llamaConfig.getApiKey()).thenReturn("test-api-key");
        when(llamaConfig.getApiUrl()).thenReturn("https://api.test.com");
        
        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("https://api.test.com"),
            eq(HttpMethod.POST),
            any(),
            eq(String.class)
        )).thenReturn(responseEntity);

        // Execute
        String response = llamaService.processText(question);

        // Verify
        assertNotNull(response);
        assertTrue(response.contains("verse teaches"));
    }
}
