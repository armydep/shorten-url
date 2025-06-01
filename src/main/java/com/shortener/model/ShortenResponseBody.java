package com.shortener.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShortenResponseBody {
    private String shortUrl;
    private String code;
}
