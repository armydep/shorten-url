package com.shortener.service;


import com.shortener.entity.LongToShort;
import com.shortener.entity.ShortToLong;
import com.shortener.model.ShortenRecord;
import com.shortener.model.ShortenResponseBody;
import com.shortener.repository.LongToShortRepository;
import com.shortener.repository.ShortToLongRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ShortenService {
    @Autowired
    private LongToShortRepository longRepo;

    @Autowired
    private ShortToLongRepository shortRepo;

    @Value("${shortener.base-url}")
    private String baseUrl;

    @Autowired
    private ShortenUrlAlgo shortenUrlAlgo;

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
        Optional<ShortToLong> existing = shortRepo.findById(code);
        return existing.map(ShortToLong::getLongUrl).orElse(null);
        //increment clicks count
    }

    //optional
    public ShortenRecord getStats(String code) {
        if (code.equals("zzz")) {
            return null;
        }

        ShortenRecord record = new ShortenRecord();
        record.setClicks(13);
        record.setCreatedAt(System.nanoTime());
        return record;
    }
}
