package com.shortener.service;


import org.springframework.stereotype.Service;

@Service
public class ShortenService {

    public String shorten(String longUrl){
        return "a-ok-short-svc";
    }

    public String getLongUrl(String code){
        return "a-ok-long-svc";
    }
}
