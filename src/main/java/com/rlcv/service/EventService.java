package com.rlcv.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rlcv.dto.EventRequest;
import com.rlcv.model.Event;
import com.rlcv.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    // create a new event and record to db
    public String createEvent(EventRequest request, String ipAddress, UUID owner) {
        
        Event event = Event.builder()
                .url(request.getUrl())
                .eventType(request.getEventType())
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .owner(owner)
                .build();

        eventRepository.save(event);

        String counterKey = CacheKeys.totalCount(request.getUrl(), owner,LocalDate.now());
        redisTemplate.opsForValue().increment(counterKey);

        eventPublisher.publish(event);

        return ("Event ID: " + event.getId());
    }

    // get an event
    public Event getAnEvent(UUID id) {
        return (eventRepository.findById(id).orElseThrow());
    }
}
