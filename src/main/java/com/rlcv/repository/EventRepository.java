package com.rlcv.repository;

import com.rlcv.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {
    // check if url exists
    boolean existsByUrl(String url);

    // return the details of all occurence of selected event
    List<Event> findByUrlAndEventTypeAndTimestampBetweenAndTimestampLessThan(
        String url,
        String eventType,
        LocalDateTime start,
        LocalDateTime end,
        LocalDateTime cursor,
        Pageable pageable
    );

    // return the last 15 events recorded
    List<Event> findTop15ByUrlOrderByTimestampDesc(String url);

    // total events for a url on a given day or within a time range
    long countByUrlAndTimestampBetween(String url, LocalDateTime start, LocalDateTime end);

    // total events for a url by an event on a given day or within a time range
    long countByUrlAndEventTypeAndTimestampBetween(String url, String eventType, LocalDateTime start, LocalDateTime end);

    // all events for a url on a given day, grouped by event type
    @Query("SELECT e.eventType, COUNT(e) FROM Event e WHERE e.url = :url " +
            "AND e.timestamp BETWEEN :start AND :end GROUP BY e.eventType")
    List<Object[]> countGroupedByEventType(
        @Param("url") String url,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // top url for a event count on a given day, in desc order
    @Query("SELECT e.url, COUNT(e) FROM Event e WHERE e.timestamp BETWEEN :start AND :end " +
            "GROUP BY e.url ORDER BY COUNT(e) DESC")
    List<Object[]> findTopUrls(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    // events per hour for a url on a given day
    @Query("SELECT HOUR(e.timestamp), COUNT(e) FROM Event e WHERE e.url = :url " +
            "AND e.timestamp BETWEEN :start AND :end GROUP BY HOUR(e.timestamp) " +
            "ORDER BY HOUR(e.timestamp)")
    List<Object[]> countPerHour(
        @Param("url") String url,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // count all events for a domain regardless of path
    long countByUrlStartingWithAndTimestampBetween(
        String urlPrefix, LocalDateTime start, LocalDateTime end
    );

    // show raw events for a domain
    Page<Event> findByUrlStartingWithAndEventTypeAndTimestampBetween(
        String urlPrefix, String eventType, LocalDateTime start, LocalDateTime end, LocalDateTime cursor, Pageable pageable
    );

}
