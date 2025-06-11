package com.shortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortener.entity.UrlMapping;
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
    void testShortenOk() throws Exception {
        ShortenRequestBody requestBody = new ShortenRequestBody();
        requestBody.setUrl("https://example.com");
        ShortenResponseBody responseBody = new ShortenResponseBody();
        responseBody.setShortUrl("https://short.ly/abc123");
        when(shortenService.shorten("https://example.com")).thenReturn(responseBody);
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortUrl").value("https://short.ly/abc123"));
    }

    @Test
    void testShortenNonValid() throws Exception {
        ShortenRequestBody requestBody = new ShortenRequestBody();
        requestBody.setUrl("");
        ShortenResponseBody responseBody = new ShortenResponseBody();
        responseBody.setShortUrl("https://short.ly/abc123");
        when(shortenService.shorten("https://example.com")).thenReturn(responseBody);
        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testRedirectOk() throws Exception {
        String code = "abc123";
        String longUrl = "https://example.com";
        when(shortenService.getLongUrl(code)).thenReturn(longUrl);
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));
    }

    @Test
    void testRedirectNotFound() throws Exception {
        String code = "invalid";
        when(shortenService.getLongUrl(code)).thenReturn(null);
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void testRedirectEmptyCode() throws Exception {
        String code = "";
        when(shortenService.getLongUrl(anyString())).thenReturn(null);
        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStatsOk() throws Exception {
        String code = "abc123";
        UrlMapping record = new UrlMapping();
        record.setClicks(12L);
        record.setCreatedAt(1000L);
        when(shortenService.getShortToLongEntry(code, false)).thenReturn(record);
        mockMvc.perform(get("/api/stats/{code}", code))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clicks").value(12))
                .andExpect(jsonPath("$.createdAt").value(1000L));
    }

    @Test
    void testStatsNotFound() throws Exception {
        String code = "invalid";
        when(shortenService.getShortToLongEntry(code, false)).thenReturn(null);
        mockMvc.perform(get("/api/stats/{code}", code))
                .andExpect(status().isNotFound());
    }

}