package com.shortener.model;

import lombok.Data;

@Data
public class ShortenRecord {
    private final Integer clicks;
    private final Long createdAt;
}
