package com.shortener.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("long_to_short")
@Data
public class LongToShort {
    @PrimaryKey
    private String longUrl;
    private String shortUrl;
    private Long createdAt;
}