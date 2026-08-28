package com.rlcv.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventRequest {
    @NotBlank(message = "url is required")
    private String url;

    @NotBlank(message = "event type is required")
    private String eventType;
}


