package com.shortener.controller;

import com.shortener.entity.UrlMapping;
import com.shortener.model.ShortenRequestBody;
import com.shortener.model.ShortenResponseBody;
import com.shortener.service.ShortenService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShortenController {
    private final ShortenService service;

    /**
     * Creates a shortened URL for the provided long URL.
     *
     * <p>Endpoint: POST /api/shorten</p>
     *
     * @param body the request body containing the long URL to shorten.
     * @return HTTP 200 OK with {@link ShortenResponseBody} containing the short code and shortened URL,
     *         or HTTP 400 Bad Request if the input is invalid.
     */
    @PostMapping(value = "/api/shorten", consumes = "application/json")
    public ResponseEntity<ShortenResponseBody> shorten(@RequestBody ShortenRequestBody body) {
        if (isValid(body)) {
            ShortenResponseBody response = service.shorten(body.getUrl());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Redirects to the original long URL corresponding to the provided short code.
     *
     * <p>Endpoint: GET /{code}</p>
     *
     * @param code the short URL code to resolve.
     * @return HTTP 301 Moved Permanently with a Location header pointing to the original URL,
     *         or HTTP 404 Not Found if the code is not recognized.
     */
    @GetMapping(value = "/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = service.getLongUrl(code);
        if (longUrl == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    /**
     * Retrieves statistics for the given short URL code, such as the original URL,
     * number of clicks, and creation timestamp.
     *
     * <p>Endpoint: GET /api/stats/{code}</p>
     *
     * @param code the short URL code to look up.
     * @return HTTP 200 OK with {@link UrlMapping} details,
     *         or HTTP 404 Not Found if the code is unknown.
     */
    @GetMapping(value = "/api/stats/{code}")
    public ResponseEntity<UrlMapping> stats(@PathVariable String code) {
        UrlMapping record = service.getShortToLongEntry(code, false);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * Validates that the given request body contains a non-null, non-empty URL.
     *
     * @param body the {@link ShortenRequestBody} to validate.
     * @return true if the URL is valid; false otherwise.
     */
    public boolean isValid(@NonNull ShortenRequestBody body) {
        return body.getUrl() != null && !body.getUrl().isEmpty();
    }
}