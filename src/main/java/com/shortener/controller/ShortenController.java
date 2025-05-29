package com.shortener.controller;

import com.shortener.model.ShortenRecord;
import com.shortener.model.ShortenRequestBody;
import com.shortener.model.ShortenResponseBody;
import com.shortener.service.ShortenService;
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
        ShortenResponseBody response = new ShortenResponseBody(body.getUrl(), "okey: " + body.getUrl());
        return ResponseEntity.ok(response);//surveyService.createSurvey(survey));
    }

    @GetMapping(value = "/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(service.getLongUrl(code)));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping(value = "/api/stats/{code}")
    public ResponseEntity<ShortenRecord> stats(@PathVariable String code) {
        ShortenRecord record = new ShortenRecord(13, System.nanoTime());
        return ResponseEntity.ok(record);
    }
}