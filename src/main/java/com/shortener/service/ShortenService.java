package com.shortener.service;


import com.datastax.oss.driver.shaded.guava.common.annotations.VisibleForTesting;
import com.shortener.entity.ClicksCount;
import com.shortener.entity.LongToShort;
import com.shortener.entity.ShortToLong;
import com.shortener.model.ShortenResponseBody;
import com.shortener.repository.ClicksCountRepository;
import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ShortenService {
    private final LongToShortRepository longRepo;
    private final ShortToLongRepository shortRepo;
    private final ClicksCountRepository clicksCountRepository;
    private final CodeGenerator shortenUrlAlgo;
    private final CacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${shortener.base-url}")
    private String baseUrl;

    @VisibleForTesting
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ShortenResponseBody shorten(@NotBlank String longUrl) {
        Optional<LongToShort> existing = longRepo.findById(longUrl);
        if (existing.isPresent()) {
            LongToShort longToShort = existing.get();
            ShortenResponseBody body = new ShortenResponseBody();
            body.setShortUrl(generateShortUrl(baseUrl, longToShort.getCode()));
            body.setCode(longToShort.getCode());
            return body;
        }
        String code = shortenUrlAlgo.generate(longUrl);
        long timestamp = Instant.now().getEpochSecond();

        LongToShort lts = new LongToShort();
        lts.setLongUrl(longUrl);
        lts.setCode(code);
        lts.setCreatedAt(timestamp);

        ShortToLong stl = new ShortToLong();
        stl.setCode(code);
        stl.setLongUrl(longUrl);
        stl.setCreatedAt(timestamp);

        ClicksCount clicksCount = new ClicksCount();
        clicksCount.setCode(code);

        clicksCountRepository.incrementCounter(code);
        clicksCountRepository.decrementCounter(code);
        longRepo.save(lts);
        shortRepo.save(stl);

        ShortenResponseBody body = new ShortenResponseBody();
        body.setCode(code);
        body.setShortUrl(generateShortUrl(baseUrl, code));
        return body;
    }

    private String generateShortUrl(String baseUrl, String code) {
        return baseUrl + "/" + code;
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
            Optional<ClicksCount> existingClicks = clicksCountRepository.findById(code);
            if (existing.isPresent()) {
                if (existingClicks.isEmpty()) {
                    return null;
                }
                ClicksCount clicksVal = existingClicks.get();
                ShortToLong val = existing.get();
                val.setClicksCount(clicksVal.getCount());
                if (updateCount) {
                    val.incrementCount();
                    eventPublisher.publishEvent(val);
                }
                cacheService.put(code, val);
                return val;
            }
            return null;
        } else {
            if (updateCount) {
                cached.incrementCount();
                eventPublisher.publishEvent(cached);
                cacheService.put(code, cached);
            }
            return cached;
        }
    }

    @Transactional
    public void incrementCounts(ShortToLong event) {
        clicksCountRepository.incrementCounter(event.getCode());
    }
}
