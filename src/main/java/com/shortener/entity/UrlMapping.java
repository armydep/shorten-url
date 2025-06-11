package com.shortener.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "url_mappings")
public class UrlMapping {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    @Indexed
    private String longUrl;

    private Long createdAt;
    private Long clicks = 0L;


    public void incrementCount() {
        clicks++;
    }
}