package com.shortener.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("short_to_long")
@Data
public class ShortToLong {
    @PrimaryKey
    private String shortUrl;

    private String longUrl;
    private Long createdAt;
    private Long clicksCount;
}