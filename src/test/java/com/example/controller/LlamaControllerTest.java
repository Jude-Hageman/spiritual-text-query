package com.example.controller;

import com.example.service.LlamaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LlamaController.class)
public class LlamaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlamaService llamaService;

    @Test
    public void processText_ShouldReturnResponse() throws Exception {
        // Given
        String inputText = "Can you explain John 3:16?";
        String expectedResponse = "Here's an explanation of John 3:16...";
        when(llamaService.processText(anyString())).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/llama/process")
                .contentType(MediaType.TEXT_PLAIN)
                .content(inputText))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void processText_WithEmptyInput_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/llama/process")
                .contentType(MediaType.TEXT_PLAIN)
                .content(""))
                .andExpect(status().isBadRequest());
    }
}
