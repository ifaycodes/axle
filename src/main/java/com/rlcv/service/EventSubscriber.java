package com.rlcv.service;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rlcv.controller.AnalyticsController;
import com.rlcv.model.Event;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventSubscriber implements MessageListener{

    private final AnalyticsController analyticsController;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Event event = objectMapper.readValue(message.getBody(), Event.class);
            analyticsController.pushToFeed(event);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process event", e);
        }
    }
}

