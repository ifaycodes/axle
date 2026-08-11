package com.rlcv.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class HourlyBreakdown {
    private int hour;
    private long count;
}
