package com.rlcv.repository;

import com.rlcv.model.Event;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {
    boolean existsByUrl(String url);
    long countByUrlAndDate(String url, LocalDate date);
    long countByUrlAndEventTypeAndDate(String url, String eventType, LocalDate date);
}
