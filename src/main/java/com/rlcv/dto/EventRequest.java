package com.rlcv.dto;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class EventRequest {
    @NotBlank(message = "url is required")
    private String url;

    @NotBlank(message = "event type is required")
    private String eventType;
}


