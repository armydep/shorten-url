package com.shortener.async;

import com.shortener.entity.UrlMapping;
import com.shortener.service.ShortenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IncrementEventListener {
    @Autowired
    private ShortenService service;

    @Async
    @EventListener
    public void handleIncrementEvent(UrlMapping event) {
        log.info("Increment async event {}: ", event);
        service.incrementCounts(event);
    }
}
