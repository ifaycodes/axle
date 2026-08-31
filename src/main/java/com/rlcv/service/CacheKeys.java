package com.rlcv.service;

import java.time.LocalDate;

public class CacheKeys {

    // event counter - per url, eventtype, date
    public static String auth(String hashKey) {
        return "key:auth:" + hashKey;
    }

    // total count - per url, date (no event type)
    public static String totalCount(String url, LocalDate date) {
        return "cache:total:" + url + ":" + date;
    }

    // event type breakdown - per url, date
    public static String breakdown(String url, LocalDate date) {
        return "cache:breakdown:" + url + ":" + date;
    }

    // top urls - per date only
    public static String topUrls(LocalDate date) {
        return "cache:topurls:" + date;
    }

    // hourly breakdown - per url, date
    public static String hourly(String url, LocalDate date) {
        return "cache:hourly:" + url + ":" + date;
    }

    // event type query cache - per url, eventtype, date
    public static String eventTypeQuery(String url, String eventType, LocalDate date) {
        return "cache:eventtype:" + url + ":" + eventType + ":" + date;
    }

}
