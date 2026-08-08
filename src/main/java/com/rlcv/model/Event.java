package com.rlcv.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @NotNull
    private String url;

    private String eventType;

    private LocalDateTime timestamp;

    @NotNull
    private String ipAddress;

}
