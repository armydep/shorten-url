package com.shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class ShortenURLApp {
    public static void main(String[] args) {
        SpringApplication.run(ShortenURLApp.class, args);
    }
}