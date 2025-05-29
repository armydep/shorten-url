package com.shortener.service;


import com.shortener.model.ShortenRecord;
import com.shortener.model.ShortenResponseBody;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ShortenService {

    public ShortenResponseBody shorten(String longUrl) {
        ShortenResponseBody response = new ShortenResponseBody(longUrl, "okey: " + longUrl);
        return response;
    }

    public String getLongUrl(@NonNull String code) {
        if (code.equals("zzz")) {
            return null;
        }
        return "a-ok-long-svc";
    }

    public ShortenRecord getStats(String code) {
        if (code.equals("zzz")) {
            return null;
        }
        return new ShortenRecord(13, System.nanoTime());
    }
}
