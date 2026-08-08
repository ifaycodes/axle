package com.rlcv.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponse {
    private String url;
    private String eventType;
    private long count;
    private String date;
}
