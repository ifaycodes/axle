package com.rlcv.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rlcv.dto.EventRequest;
import com.rlcv.model.ApiKey;
import com.rlcv.model.Event;
import com.rlcv.service.ApiKeyService;
import com.rlcv.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final ApiKeyService apiKeyService;

    @PostMapping
    @Operation(summary = "Record an event to the db and push to live feed")
    public ResponseEntity<String> recordEvent (@RequestBody @Valid EventRequest request, HttpServletRequest httpRequest) {
        ApiKey apiKey = (ApiKey) httpRequest.getAttribute("apiKey");
        String ipAddress = httpRequest.getRemoteAddr();

        if (!apiKeyService.ownsUrl(apiKey, request.getUrl())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UUID ownerId = apiKey.getId();
        return ResponseEntity.ok(eventService.createEvent(request, ipAddress, ownerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get the details of a particular event")
    public ResponseEntity<Event> getEventDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.getAnEvent(id));
    }
}
