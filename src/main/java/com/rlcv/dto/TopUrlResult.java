package com.rlcv.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TopUrlResult {
    private String url;
    private long count;
}
