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

    /**
     * Generates a short URL code for a given long URL.
     * <p>
     * If the long URL already exists in the database, its existing code is reused.
     * Otherwise, a new short code is generated, persisted in both directions (long → short and short → long),
     * and click count tracking is initialized.
     *
     * @param longUrl the original long URL to shorten (must be non-null and non-blank)
     * @return a response object containing the generated or existing short code and full short URL
     */
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

    /**
     * Retrieves the original long URL for a given short code.
     * <p>
     * Delegates to {@link #getShortToLongEntry(String, boolean)} and returns only the long URL string.
     * May return {@code null} if the code is not found.
     *
     * @param code the short URL code
     * @return the original long URL, or null if not found
     */
    public String getLongUrl(@NotBlank String code) {
        ShortToLong cached = getShortToLongEntry(code, true);
        if (cached == null) {
            return null;
        }
        return cached.getLongUrl();
    }

    /**
     * Resolves a short code to its full mapping entity, optionally updating click statistics.
     * <p>
     * This method is invoked by two endpoints:
     * <ul>
     *   <li><b>Redirect endpoint</b> (<code>GET /{code}</code>): sets <code>updateCount = true</code> to increment the click counter and publish an event.</li>
     *   <li><b>Statistics endpoint</b> (<code>GET /api/stats/{code}</code>): sets <code>updateCount = false</code> to retrieve the entity without modifying the click count.</li>
     * </ul>
     * <p>
     * First attempts to retrieve the mapping from cache. If not found, it queries the database.
     * If both the mapping and its associated click counter exist, a {@link ShortToLong} object is returned.
     *
     * @param code the short URL code to resolve
     * @param updateCount if {@code true}, increments the click counter and publishes an event asynchronously; otherwise leaves the count unchanged
     * @return the resolved {@link ShortToLong} entity with metadata and click count, or {@code null} if not found
     */
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

    /**
     * Constructs the full shortened URL by appending the code to the base URL.
     *
     * @param baseUrl the base URL (e.g. https://sho.rt)
     * @param code the unique short code
     * @return a concatenated string forming the complete short URL
     */
    private String generateShortUrl(String baseUrl, String code) {
        return baseUrl + "/" + code;
    }

    /**
     * Increments the persistent click counter for a given short code.
     * <p>
     * Called asynchronously when a redirection event occurs.
     *
     * @param event the event containing the short URL code to increment
     */
    @Transactional
    public void incrementCounts(ShortToLong event) {
        clicksCountRepository.incrementCounter(event.getCode());
    }
}
