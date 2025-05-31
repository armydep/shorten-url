package com.shortener.service;


import com.shortener.entity.LongToShort;
import com.shortener.entity.ShortToLong;
import com.shortener.model.ShortenResponseBody;
import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class ShortenService {
    @Autowired
    private LongToShortRepository longRepo;

    @Autowired
    private ShortToLongRepository shortRepo;

    @Value("${shortener.base-url}")
    private String baseUrl;

    @Autowired
    private CodeGenerator shortenUrlAlgo;

    @Autowired
    private CacheService cacheService;

    public ShortenResponseBody shorten(@NotBlank String longUrl) {
        Optional<LongToShort> existing = longRepo.findById(longUrl);
        if (existing.isPresent()) {
            LongToShort longToShort = existing.get();
            return new ShortenResponseBody(longToShort.getShortUrl(), longToShort.getLongUrl());
        }
        String code = shortenUrlAlgo.generate(longUrl);
        long timestamp = Instant.now().getEpochSecond();

        LongToShort lts = new LongToShort();
        lts.setLongUrl(longUrl);
        lts.setShortUrl(code);
        lts.setCreatedAt(timestamp);
        lts.setClicksCount(0L);

        ShortToLong stl = new ShortToLong();
        stl.setShortUrl(code);
        stl.setLongUrl(longUrl);
        stl.setCreatedAt(timestamp);
        stl.setClicksCount(0L);

        longRepo.save(lts);
        shortRepo.save(stl);

        return new ShortenResponseBody(lts.getShortUrl(), lts.getLongUrl());
    }

    public String getLongUrl(@NotBlank String code) {
        ShortToLong cached = getShortToLongEntry(code, true);
        if (cached == null) {
            return null;
        }
        return cached.getLongUrl();
    }

    public ShortToLong getShortToLongEntry(@NotBlank String code, boolean updateCount) {
        ShortToLong cached = cacheService.get(code);
        log.info("Cached value: {}", cached);
        if (cached == null) {
            Optional<ShortToLong> existing = shortRepo.findById(code);
            if (existing.isPresent()) {
                ShortToLong val = existing.get();
                if (updateCount) {
                    val.incrementCount();
                }
                cacheService.put(code, val);
                return val;
            }
            return null;
        } else {
            if (updateCount) {
                cached.incrementCount();
            }
            return cached;
        }
    }
}
