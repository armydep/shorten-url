package com.shortener.service;

import com.shortener.entity.ClicksCount;
import com.shortener.entity.LongToShort;
import com.shortener.entity.ShortToLong;
import com.shortener.model.ShortenResponseBody;
import com.shortener.repository.ClicksCountRepository;
import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortenServiceTest {

    private LongToShortRepository longRepo;
    private ShortToLongRepository shortRepo;
    private ClicksCountRepository clicksRepo;
    private CodeGenerator generator;
    private CacheService cacheService;
    private ApplicationEventPublisher eventPublisher;

    private ShortenService shortenService;
    private final String BASE_URL = "https://sho.rt";

    @BeforeEach
    void setUp() {
        longRepo = mock(LongToShortRepository.class);
        shortRepo = mock(ShortToLongRepository.class);
        clicksRepo = mock(ClicksCountRepository.class);
        generator = mock(CodeGenerator.class);
        cacheService = mock(CacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        shortenService = new ShortenService(longRepo, shortRepo, clicksRepo, generator, cacheService, eventPublisher);
        shortenService.setBaseUrl(BASE_URL);
    }

    @Test
    void testShortenReturnsExistingMappingIfExists() {
        LongToShort existing = new LongToShort();
        String code = "abc123";
        String longUrl = "https://example.com";
        existing.setCode(code);
        existing.setLongUrl(longUrl);
        when(longRepo.findById(longUrl)).thenReturn(Optional.of(existing));
        ShortenResponseBody result = shortenService.shorten(longUrl);
        assertEquals(code, result.getCode());
        assertEquals(generateShortUrl(BASE_URL, code), result.getShortUrl());
        verify(generator, never()).generate(any());
    }

    @Test
    void testShortenCreatesNewMapping() {
        String code = "xyz789";
        String longUrl = "https://new.com";
        when(longRepo.findById(anyString())).thenReturn(Optional.empty());
        when(generator.generate(anyString())).thenReturn(code);
        ShortenResponseBody result = shortenService.shorten(longUrl);
        assertEquals(code, result.getCode());
        assertEquals(generateShortUrl(BASE_URL, code), result.getShortUrl());
        verify(clicksRepo).incrementCounter(code);
        verify(clicksRepo).decrementCounter(code);
        verify(longRepo).save(any(LongToShort.class));
        verify(shortRepo).save(any(ShortToLong.class));
    }

    @Test
    void testGetLongUrlReturnsFromCache() {
        String code = "abc123";
        String longUrl = "https://example.com";
        ShortToLong cached = new ShortToLong();
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
        ShortToLong entity = new ShortToLong();
        entity.setCode(code);
        entity.setLongUrl(longUrl);
        ClicksCount count = new ClicksCount();
        count.setCode(code);
        count.setCount(5L);
        when(shortRepo.findById(code)).thenReturn(Optional.of(entity));
        when(clicksRepo.findById(code)).thenReturn(Optional.of(count));
        String result = shortenService.getLongUrl(code);
        assertEquals(longUrl, result);
        verify(cacheService).put(eq(code), any(ShortToLong.class));
        verify(eventPublisher).publishEvent(any(ShortToLong.class));
    }

    @Test
    void testGetLongUrlReturnsNullWhenNotFound() {
        when(cacheService.get("missing")).thenReturn(null);
        when(shortRepo.findById("missing")).thenReturn(Optional.empty());
        String result = shortenService.getLongUrl("missing");
        assertNull(result);
    }

    private String generateShortUrl(String baseUrl, String code) {
        return baseUrl + "/" + code;
    }
}