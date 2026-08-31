package com.rlcv.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.rlcv.dto.AnalyticsResponse;
import com.rlcv.dto.EventTypeBreakdown;
import com.rlcv.dto.HourlyBreakdown;
import com.rlcv.dto.TopUrlResult;
import com.rlcv.exceptions.AccessDeniedException;
import com.rlcv.model.ApiKey;
import com.rlcv.model.Event;
import com.rlcv.service.AnalyticsService;
import com.rlcv.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Endpoints", description = "Query for Analysis")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ApiKeyService apiKeyService;
    private Map<String, List<SseEmitter>> emittersByUrl = new ConcurrentHashMap<>();

    @GetMapping("/")
    @Operation(summary = "Return all recorded events")
    public ResponseEntity<Page<Event>> getRecordedEvents(
        @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(analyticsService.getAllEvent(size));
    }

    @GetMapping("/{url}")
    @Operation(summary = "Get total count of visit to a url")
    public ResponseEntity<AnalyticsResponse> getTotalCountOnAUrl(
            @PathVariable String url, 
            @RequestParam(required = false) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);
        
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(analyticsService.getTotalCountOnDomain(url, queryDate));
    }

    @GetMapping("/{url}/eventtype")
    @Operation(summary = "Get counts of an event on a url")
    public ResponseEntity<AnalyticsResponse> getTotalCountOfEventType(
            @PathVariable String url,
            @RequestParam String eventType,
            @RequestParam(required = false) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);

        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(analyticsService.getCountByEventType(url, eventType, queryDate));
    }

    @GetMapping("/{url}/breakdown")
    @Operation(summary = "Get counts of all present events on a url")
    public ResponseEntity<List<EventTypeBreakdown>> getTotalCountForEachEventBreakdown(
            @PathVariable String url,
            @RequestParam(required = false) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);

        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(analyticsService.getEventypeBreakdown(url, queryDate));
    }

    @GetMapping("/top")
    @Operation(summary = "Get list of top urls in descending order")
    public ResponseEntity<List<TopUrlResult>> getTopUrls(
            @RequestParam(required = false) LocalDate date
    ) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(analyticsService.getTopUrls(queryDate));
    }

    @GetMapping("/{url}/hourly")
    @Operation(summary = "Get a breakdown of hourly activities on a url")
    public ResponseEntity<List<HourlyBreakdown>> getHourlyBreakdown(
            @PathVariable String url,
            @RequestParam(required = false) LocalDate date,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);

        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(analyticsService.getHourlyBreakdown(url, queryDate));
    }

    @GetMapping("/{url}/eventtype/details")
    @Operation(summary = "Get the details of a returned event, time, id, url etc")
    public ResponseEntity<List<Event>> getEventDetails(
            @PathVariable String url,
            @RequestParam String eventType,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);

        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalDateTime startingCursor = cursor != null ? cursor : queryDate.atTime(LocalTime.MAX);
        return ResponseEntity.ok(analyticsService.getEventDetails(url, eventType, queryDate, startingCursor, size));
    }

    @GetMapping("/{url}/details")
    @Operation(summary = "Get the details of events for a url")
    public ResponseEntity<Page<Event>> getDetailsOfAllEventsOfAUrl(
            @PathVariable String url,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest
    ) {
        checkOwnership(httpRequest, url);

        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalDateTime startingCursor = cursor != null ? cursor : queryDate.atTime(LocalTime.MAX);
        return ResponseEntity.ok(analyticsService.getEventDetailsByDomain(url, queryDate, startingCursor, size));
    }

    @Operation(summary = "Constant feed of the last 15 events in the db")
    @GetMapping(value = "/feed", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter liveFeed(@RequestParam String url, HttpServletRequest httpRequest) {
        
        checkOwnership(httpRequest, url);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); 
        emittersByUrl.computeIfAbsent(url, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // clean up when there is a disconnect
        emitter.onCompletion(() -> emittersByUrl.get(url).remove(emitter));
        emitter.onTimeout(() -> emittersByUrl.get(url).remove(emitter));

        return emitter;
    }

    public void pushToFeed (Event event) {
        List<SseEmitter> emitters = emittersByUrl.get(event.getUrl());
        if (emitters == null) return;
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(event));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }



    private void checkOwnership(HttpServletRequest httpRequest, String url) {
        ApiKey apiKey = (ApiKey) httpRequest.getAttribute("apiKey");

        if (!apiKeyService.ownsUrl(apiKey, url)) {
            throw new AccessDeniedException("You do not own this URL");
        }
    }
}
