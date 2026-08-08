package com.rlcv.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.rlcv.dto.EventRequest;
import com.rlcv.model.Event;
import com.rlcv.repository.EventRepository;

@Service
public class EventService {
    private EventRepository eventRepository;
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

        String counterKey = CacheKeys.counterKey(request.getUrl(), LocalDate.now());
        redisTemplate.opsForValue().increment(counterKey);

        return ("Event recorded: " + event);
    }

    // get a list of all events
    public List<Event> getEvents() {
        return (eventRepository.findAll());
    }

    public Event getOneEvent(UUID id) {
        return (eventRepository.findById(id).orElseThrow());
    }

    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }

    public String buildCounterKey(String url) {
        String today = LocalDate.now().toString();
        return "counter:" + url + ":" + today;
    }
}
