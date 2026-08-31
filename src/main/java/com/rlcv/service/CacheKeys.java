package com.rlcv.service;

import java.time.LocalDate;
import java.util.UUID;

public class CacheKeys {

    // event counter - per url, eventtype, date
    public static String auth(String hashKey) {
        return "key:auth:" + hashKey;
    }

    // total count - per url, date (no event type)
    public static String totalCount(String url, UUID owner, LocalDate date) {
        return "cache:total:" + url + ":" + owner + ":" + date;
    }

    // event type breakdown - per url, date
    public static String breakdown(String url, UUID owner, LocalDate date) {
        return "cache:breakdown:" + url + ":" + owner + ":" + date;
    }

    // top urls - per date only
    public static String topUrls(UUID owner, LocalDate date) {
        return "cache:topurls:" + owner + ":" + date;
    }

    // hourly breakdown - per url, date
    public static String hourly(String url, UUID owner, LocalDate date) {
        return "cache:hourly:" + url + ":" + owner + ":" + date;
    }

    // event type query cache - per url, eventtype, date
    public static String eventTypeQuery(String url, UUID owner, String eventType, LocalDate date) {
        return "cache:eventtype:" + url + ":" + owner + ":" + eventType + ":" + date;
    }

}
