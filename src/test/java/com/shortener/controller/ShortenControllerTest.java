package com.shortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortener.model.ShortenRecord;
import com.shortener.model.ShortenRequestBody;
import com.shortener.model.ShortenResponseBody;
import com.shortener.service.ShortenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortenController.class)
class ShortenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortenService shortenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shorten_ValidRequest_ReturnsOk() throws Exception {
        // Given
        ShortenRequestBody requestBody = new ShortenRequestBody();
        requestBody.setUrl("https://example.com");

        ShortenResponseBody responseBody = new ShortenResponseBody();
        responseBody.setShortUrl("https://short.ly/abc123");

        when(shortenService.shorten("https://example.com")).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortUrl").value("https://short.ly/abc123"));
    }


    @Test
    void redirect_ValidCode_ReturnsMovedPermanently() throws Exception {
        // Given
        String code = "abc123";
        String longUrl = "https://example.com";

        when(shortenService.getLongUrl(code)).thenReturn(longUrl);

        // When & Then
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));
    }

    @Test
    void redirect_InvalidCode_ReturnsNotFound() throws Exception {
        // Given
        String code = "invalid";

        when(shortenService.getLongUrl(code)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirect_EmptyCode_ReturnsNotFound() throws Exception {
        // Given
        String code = "";

        when(shortenService.getLongUrl(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void stats_ValidCode_ReturnsOk() throws Exception {
        // Given
        String code = "abc123";
        ShortenRecord record = new ShortenRecord();
        record.setClicks(12);
        record.setCreatedAt(1000L);

        when(shortenService.getStats(code)).thenReturn(record);

        // When & Then
        mockMvc.perform(get("/api/stats/{code}", code))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clicks").value(12))
                .andExpect(jsonPath("$.createdAt").value(1000L));
    }

    @Test
    void stats_InvalidCode_ReturnsNotFound() throws Exception {
        // Given
        String code = "invalid";

        when(shortenService.getStats(code)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/stats/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void stats_EmptyCode_ReturnsNotFound() throws Exception {
        // Given
        String code = "";

        when(shortenService.getStats(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/stats/{code}", code))
                .andExpect(status().isNotFound());
    }
}