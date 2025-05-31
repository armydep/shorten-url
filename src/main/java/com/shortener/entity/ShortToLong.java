package com.shortener.entity;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.io.Serializable;

@Table("short_to_long")
@Data
public class ShortToLong implements Serializable {
    @PrimaryKey
    private String shortUrl;
    private String longUrl;
    private Long createdAt;
    @Transient
    private Long clicksCount;

    public void incrementCount() {
        if (clicksCount == null) {
            clicksCount = 0L;
        }
        clicksCount++;
    }
}