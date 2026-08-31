package com.rlcv.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rlcv.dto.AnalyticsResponse;
import com.rlcv.dto.EventTypeBreakdown;
import com.rlcv.dto.HourlyBreakdown;
import com.rlcv.dto.TopUrlResult;
import com.rlcv.model.Event;
import com.rlcv.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventRepository eventRepository;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;

    // get all recoded events - used pageable to return 50 rows
    public Page<Event> getAllEvent(int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("timestamp", "id").descending());
        return eventRepository.findAllEvent(pageable);
    }

    // return event details
    public List<Event> getEventDetails(String url, String eventType, LocalDate date, LocalDateTime cursor, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("timestamp", "id").descending());
        return eventRepository.findByUrlStartingWithAndEventTypeAndTimestampBetweenAndTimestampLessThan(
            url, eventType, date.atStartOfDay(), date.atTime(LocalTime.MAX), cursor, pageable);
    }

    // keep live feed of db changes
    // might have to remove this
    public List<Event> getLiveFeed(String url) {
        return eventRepository.findTop15ByUrlOrderByTimestampDesc(url);
    }

    // get total count for a url on a date
    // might have to remove this
    public AnalyticsResponse getTotalCount(String url, LocalDate date) {
        String cacheKey = CacheKeys.totalCount(url, date);

        return getCachedOrQuery(cacheKey, () -> {
            long count = eventRepository.countByUrlAndTimestampBetween(
                url, date.atStartOfDay(), date.atTime(LocalTime.MAX));

            return AnalyticsResponse.builder()
                    .url(url)
                    .count(count)
                    .date(date.toString())
                    .eventType("all_events")
                    .build();
        });
    }

    // get total count for a domain url
    public AnalyticsResponse getTotalCountOnDomain(String urlPrefix, LocalDate date) {
        String cacheKey = CacheKeys.totalCount(urlPrefix, date);

        return getCachedOrQuery(cacheKey, () -> {
            long count = eventRepository.countByUrlStartingWithAndTimestampBetween(
                urlPrefix, date.atStartOfDay(), date.atTime(LocalTime.MAX));

            return AnalyticsResponse.builder()
                    .url(urlPrefix)
                    .count(count)
                    .date(date.toString())
                    .eventType("all_events")
                    .build();
        });
    }

    //count by event type for a url on a date
    public AnalyticsResponse getCountByEventType(String url, String eventType, LocalDate date) {
        String cacheKey = CacheKeys.eventTypeQuery(url, eventType, date);

        return getCachedOrQuery(cacheKey, () -> {
            long count = eventRepository.countByUrlStartingWithAndEventTypeAndTimestampBetween(
                url, eventType, date.atStartOfDay(), date.atTime(LocalTime.MAX));

            return AnalyticsResponse.builder()
                    .url(url)
                    .eventType(eventType)
                    .count(count)
                    .date(date.toString())
                    .build();
        });
    }

    // breakdown by event type for a url on a date
    public List<EventTypeBreakdown> getEventypeBreakdown(String url, LocalDate date) {
        String cacheKey = CacheKeys.breakdown(url, date);
        String cached =  redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return deserializeList(cached, EventTypeBreakdown.class);
        }

        List<Object[]> results = eventRepository.countUrlStartingWithAndGroupedByEventType(
            url, date.atStartOfDay(), date.atTime(LocalTime.MAX));

        List<EventTypeBreakdown> breakdown = results.stream()
            .map(row -> EventTypeBreakdown.builder()
                    .eventType((String) row[0])
                    .count((Long) row[1])
                    .build())
            .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, serialize(breakdown), Duration.ofMinutes(5));
        return breakdown;
    }

    // top urls by event count on a date
    public List<TopUrlResult> getTopUrls(LocalDate date) {
        String cacheKey = CacheKeys.topUrls(date);
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return deserializeList(cached, TopUrlResult.class);
        }

        List<Object[]> results = eventRepository.findTopUrls(
            date.atStartOfDay(), date.atTime(LocalTime.MAX));

        List<TopUrlResult> topUrls = results.stream()
            .map(row -> TopUrlResult.builder()
                    .url((String) row[0])
                    .count((Long) row[1])
                    .build()
                )
            .collect(Collectors.toList());
        
        redisTemplate.opsForValue().set(cacheKey, serialize(topUrls), Duration.ofMinutes(5));
        return topUrls;
    }

    // events per hour for a url on a date
    public List<HourlyBreakdown> getHourlyBreakdown(String url, LocalDate date) {
        String cacheKey = CacheKeys.hourly(url, date);
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return deserializeList(cached, HourlyBreakdown.class);
        }

        List<Object[]> results = eventRepository.countUrlStartingWithAndPerHour(
            url, date.atStartOfDay(), date.atTime(LocalTime.MAX));

        List<HourlyBreakdown> hourly = results.stream()
            .map(row -> HourlyBreakdown.builder()
                    .hour((int) row[0])
                    .count((Long) row[1])
                    .build()
                )
            .collect(Collectors.toList());
        
        redisTemplate.opsForValue().set(cacheKey, serialize(hourly), Duration.ofMinutes(5));
        return hourly;
    }

    // return event details for a domain
    public Page<Event> getEventDetailsByDomain(String url, LocalDate date, LocalDateTime cursor, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("timestamp", "id").descending());
        return eventRepository.findByUrlStartingWithAndTimestampBetweenAndTimestampLessThan(
            url, date.atStartOfDay(), date.atTime(LocalTime.MAX), cursor, pageable);
    }


    // ##################### IN CLASS HELPER FUNCTIONS ############################

    private AnalyticsResponse getCachedOrQuery(String cacheKey, Supplier<AnalyticsResponse> query) {
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return deserialize(cached, AnalyticsResponse.class);
        }
        AnalyticsResponse result = query.get();
        redisTemplate.opsForValue().set(cacheKey, serialize(result), Duration.ofMinutes(5));
        return result;
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    private <T> List<T> deserializeList(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("List Deserialization failed", e);
        }
    }

}
