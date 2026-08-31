package com.rlcv.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rlcv.model.Event;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(Event event) {
        try {
            redisTemplate.convertAndSend("feed:" + event.getUrl(), 
                objectMapper.writeValueAsString(event));
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
