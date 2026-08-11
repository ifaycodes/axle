package com.rlcv.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rlcv.dto.EventRequest;
import com.rlcv.model.Event;
import com.rlcv.repository.EventRepository;

@Service
public class EventService {
    private EventRepository eventRepository;
    private EventPublisher eventPublisher;
    private RedisTemplate<String, String> redisTemplate;

    // empty constructor
    public EventService() {}

    // create a new event and record to db
    public String createEvent(EventRequest request, String ipAddress) {
        
        Event event = Event.builder()
                .url(request.getUrl())
                .eventType(request.getEventType())
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        eventRepository.save(event);

        String counterKey = CacheKeys.totalCount(request.getUrl(), LocalDate.now());
        redisTemplate.opsForValue().increment(counterKey);

        eventPublisher.publish(event);

        return ("Event recorded: " + event);
    }

    // get an event
    public Event getAnEvent(UUID id) {
        return (eventRepository.findById(id).orElseThrow());
    }
}
