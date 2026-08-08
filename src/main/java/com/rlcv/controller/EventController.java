package com.rlcv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.rlcv.dto.EventRequest;
import com.rlcv.service.EventService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

public class EventController {
    private EventService eventService;

    @PostMapping
    public ResponseEntity<Void> recordEvent (@RequestBody @Valid EventRequest request, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        eventService.createEvent(request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
