package com.rlcv.service;

import java.time.LocalDate;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rlcv.dto.AnalyticsResponse;
import com.rlcv.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private EventRepository eventRepository;
    private RedisTemplate<String,String> redisTemplate;

    public AnalyticsResponse getAnalytics(String url, String eventType, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();

        // if the eventtype is not empty, query db
        if (eventType != null) {
            long count = eventRepository.countByUrlAndEventTypeAndDate(url, eventType, queryDate);
            return AnalyticsResponse.builder()
                    .url(url)
                    .eventType(eventType)
                    .count(count)
                    .date(queryDate.toString())
                    .build();
        }

        // if event type is not added, check redis first
        String counterKey = CacheKeys.counterKey(url, queryDate);
        String cached = redisTemplate.opsForValue().get(counterKey);

        long count = cached != null 
                ? Long.parseLong(cached) 
                : eventRepository.countByUrlAndDate(url, queryDate);

        return AnalyticsResponse.builder()
                .url(url)
                .count(count)
                .date(LocalDate.now().toString())
                .build();
    }
}
