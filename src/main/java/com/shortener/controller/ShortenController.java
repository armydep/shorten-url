package com.shortener.controller;

import com.shortener.model.ShortenRecord;
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

    @PostMapping(value = "/api/shorten", consumes = "application/json")
    public ResponseEntity<ShortenResponseBody> shorten(@RequestBody ShortenRequestBody body) {
        validate(body);
        ShortenResponseBody response = service.shorten(body.getUrl());
        return ResponseEntity.ok(response);
    }

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

    @GetMapping(value = "/api/stats/{code}")
    public ResponseEntity<ShortenRecord> stats(@PathVariable String code) {
        ShortenRecord record = service.getStats(code);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    public void validate(@NonNull ShortenRequestBody body) {
        /*if (body.getUrl() == null) {

        }*/
    }
}