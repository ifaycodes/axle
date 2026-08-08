package com.rlcv.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rlcv.dto.AnalyticsResponse;
import com.rlcv.service.AnalyticsService;

public class AnalyticsController {

    private AnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable String url, @RequestParam(required = false) String eventType, @RequestParam(required = false) LocalDate date
    ) {
        return ResponseEntity.ok(analyticsService.getAnalytics(url, eventType, date));
    }
}
