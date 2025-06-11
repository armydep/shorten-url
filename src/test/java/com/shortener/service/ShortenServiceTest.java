package com.shortener.service;

import com.shortener.entity.UrlMapping;
import com.shortener.model.ShortenResponseBody;
import com.shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortenServiceTest {

    private UrlMappingRepository repository;
    private CodeGenerator generator;
    private CacheService cacheService;
    private ApplicationEventPublisher eventPublisher;

    private ShortenService shortenService;
    private final String BASE_URL = "https://sho.rt";

    @BeforeEach
    void setUp() {
        repository = mock(UrlMappingRepository.class);
        generator = mock(CodeGenerator.class);
        cacheService = mock(CacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        shortenService = new ShortenService(repository, generator, cacheService, eventPublisher);
        shortenService.setBaseUrl(BASE_URL);
    }

    @Test
    void testShortenReturnsExistingMappingIfExists() {
        UrlMapping urlMapping = new UrlMapping();
        String code = "abc123";
        String longUrl = "https://example.com";
        urlMapping.setCode(code);
        urlMapping.setLongUrl(longUrl);
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.of(urlMapping));
        ShortenResponseBody result = shortenService.shorten(longUrl);
        assertEquals(code, result.getCode());
        assertEquals(generateShortUrl(BASE_URL, code), result.getShortUrl());
        verify(generator, never()).generate(any());
    }

    @Test
    void testShortenCreatesNewMapping() {
        String code = "xyz789";
        String longUrl = "https://new.com";
        when(repository.findById(anyString())).thenReturn(Optional.empty());
        when(generator.generate(anyString())).thenReturn(code);
        ShortenResponseBody result = shortenService.shorten(longUrl);
        assertEquals(code, result.getCode());
        assertEquals(generateShortUrl(BASE_URL, code), result.getShortUrl());
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    void testGetLongUrlReturnsFromCache() {
        String code = "abc123";
        String longUrl = "https://example.com";
        UrlMapping cached = new UrlMapping();
        cached.setCode(code);
        cached.setLongUrl(longUrl);
        when(cacheService.get(code)).thenReturn(cached);
        String result = shortenService.getLongUrl(code);
        assertEquals(longUrl, result);
        verify(eventPublisher).publishEvent(cached);
        verify(cacheService).put(eq(code), any());
    }

    @Test
    void testGetLongUrlReturnsFromRepoAndCachesIt() {
        String code = "abc123";
        String longUrl = "https://example.com";
        when(cacheService.get(code)).thenReturn(null);
        UrlMapping entity = new UrlMapping();
        entity.setCode(code);
        entity.setLongUrl(longUrl);
        entity.setClicks(5L);
        when(repository.findById(code)).thenReturn(Optional.of(entity));
        String result = shortenService.getLongUrl(code);
        assertEquals(longUrl, result);
        verify(cacheService).put(eq(code), any(UrlMapping.class));
        verify(eventPublisher).publishEvent(any(UrlMapping.class));
    }

    @Test
    void testGetLongUrlReturnsNullWhenNotFound() {
        when(cacheService.get("missing")).thenReturn(null);
        when(repository.findById("missing")).thenReturn(Optional.empty());
        String result = shortenService.getLongUrl("missing");
        assertNull(result);
    }

    private String generateShortUrl(String baseUrl, String code) {
        return baseUrl + "/" + code;
    }
}