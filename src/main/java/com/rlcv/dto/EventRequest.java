package com.rlcv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EventRequest {
    @NotBlank(message = "url is required")
    private String url;

    @NotBlank(message = "event type is required")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "eventType may only contain letters, numbers, and underscores"
    )
    private String eventType;
}


