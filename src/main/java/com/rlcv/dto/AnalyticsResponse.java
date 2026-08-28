package com.rlcv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class AnalyticsResponse {
    private String url;
    private String eventType;
    private long count;
    private String date;
}
