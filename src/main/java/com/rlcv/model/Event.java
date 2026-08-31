package com.rlcv.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_url", columnList = "url"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_url_event_type", columnList = "url, event_type")
})

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private String url;

    private String eventType;

    private LocalDateTime timestamp;

    @NotNull
    private String ipAddress;

}
