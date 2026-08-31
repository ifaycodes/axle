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

    // return the details of all occurrence of selected event
    List<Event> findByUrlStartingWithAndEventTypeAndOwnerAndTimestampBetweenAndTimestampLessThan(
        String url,
        String eventType,
        UUID owner,
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
    long countByUrlStartingWithAndEventTypeAndOwnerAndTimestampBetween(String url, String eventType, UUID owner, LocalDateTime start, LocalDateTime end);

    // all events for a url on a given day, grouped by event type
    @Query("SELECT e.eventType, COUNT(e) FROM Event e WHERE owner = :owner AND e.url LIKE CONCAT(:url, '%') " +
            "AND e.timestamp BETWEEN :start AND :end GROUP BY e.eventType")
    List<Object[]> countUrlStartingWithAndGroupedByEventType(
        @Param("url") String url,
        @Param("owner") UUID owner,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // top url for a event count on a given day, in desc order
    @Query("SELECT e.url, COUNT(e) FROM Event e WHERE owner = :owner AND e.timestamp BETWEEN :start AND :end " +
            "GROUP BY e.url ORDER BY COUNT(e) DESC")
    List<Object[]> findTopUrls(
        @Param("owner") UUID owner,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    // events per hour for a url on a given day
    @Query("SELECT HOUR(e.timestamp), COUNT(e) FROM Event e WHERE owner = :owner AND e.url LIKE CONCAT(:url, '%')" +
            "AND e.timestamp BETWEEN :start AND :end GROUP BY EXTRACT (HOUR FROM e.timestamp) " +
            "ORDER BY EXTRACT (HOUR FROM e.timestamp)")
    List<Object[]> countUrlStartingWithAndPerHour(
        @Param("url") String url,
        @Param("owner") UUID owner,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    // count all events for a domain regardless of path
    long countByUrlStartingWithAndOwnerAndTimestampBetween(
        String urlPrefix, UUID owner, LocalDateTime start, LocalDateTime end
    );

    // show raw events for a domain
    Page<Event> findByUrlStartingWithAndOwnerAndTimestampBetweenAndTimestampLessThan(
        String urlPrefix, UUID owner, LocalDateTime start, LocalDateTime end, LocalDateTime cursor, Pageable pageable
    );

    // all events for a url on a given day, grouped by event type
    @Query("SELECT e.id, e.url, e.eventType, e.timestamp FROM Event e WHERE owner = :owner")
    Page<Event> findAllEvent(Pageable pageable, @Param("owner") UUID owner);
}
