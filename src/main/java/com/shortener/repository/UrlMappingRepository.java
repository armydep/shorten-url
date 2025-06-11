package com.shortener.repository;

import com.shortener.entity.UrlMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UrlMappingRepository extends MongoRepository<UrlMapping, String> {
    Optional<UrlMapping> findByCode(String shortUrl);
    Optional<UrlMapping> findByLongUrl(String longUrl);
}