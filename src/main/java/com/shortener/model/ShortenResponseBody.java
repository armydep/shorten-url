package com.shortener.model;

import lombok.Data;

@Data
public class ShortenResponseBody {
    private String shortUrl;
    private String code;
}
