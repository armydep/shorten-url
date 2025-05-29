package com.shortener.model;

import lombok.Data;

@Data
public class ShortenResponseBody {
    private final String shortUrl;
    private final String code;
}
