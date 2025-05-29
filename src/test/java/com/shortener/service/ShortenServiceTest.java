package com.shortener.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShortenServiceTest {

    @Autowired
    private ShortenService service;

    @Test
    public void testShortenAndRetrieve() {
        String longUrl = "https://example.com";
        UrlMapping mapping = service.createShortUrl(longUrl);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(longUrl, service.getByCode(mapping.getCode()).getLongUrl());
    }

    @Test
    void shorten() {

    }

    @Test
    void getLongUrl() {
    }
}