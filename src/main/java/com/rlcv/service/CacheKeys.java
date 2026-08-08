package com.rlcv.service;

import java.time.LocalDate;

public class CacheKeys {
    public static String counterKey(String url, LocalDate date) {
        return "counter:" + url + ":" + date;
    }
}
