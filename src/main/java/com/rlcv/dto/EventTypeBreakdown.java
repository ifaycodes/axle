package com.rlcv.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventTypeBreakdown {
    private String eventType;
    private long count;
}
