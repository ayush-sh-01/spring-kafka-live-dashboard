package com.analytics.engine.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * Summary aggregates for real-time dashboard and query cache.
 */
public class AnalyticsSummaryDto implements Serializable {
    private long totalEvents;
    private double averageResponseTimeMs;
    private Map<String, Long> eventsByAction;
    private Map<String, Long> eventsByCountry;
    private Map<String, Long> eventsByBrowser;
    private long l2CacheHits;
    private long l2CacheMisses;
    private long queryCacheHits;
    private long queryCacheMisses;

    public AnalyticsSummaryDto() {}

    public AnalyticsSummaryDto(long totalEvents, double averageResponseTimeMs,
                               Map<String, Long> eventsByAction,
                               Map<String, Long> eventsByCountry,
                               Map<String, Long> eventsByBrowser,
                               long l2CacheHits, long l2CacheMisses,
                               long queryCacheHits, long queryCacheMisses) {
        this.totalEvents = totalEvents;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.eventsByAction = eventsByAction;
        this.eventsByCountry = eventsByCountry;
        this.eventsByBrowser = eventsByBrowser;
        this.l2CacheHits = l2CacheHits;
        this.l2CacheMisses = l2CacheMisses;
        this.queryCacheHits = queryCacheHits;
        this.queryCacheMisses = queryCacheMisses;
    }

    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

    public double getAverageResponseTimeMs() { return averageResponseTimeMs; }
    public void setAverageResponseTimeMs(double averageResponseTimeMs) { this.averageResponseTimeMs = averageResponseTimeMs; }

    public Map<String, Long> getEventsByAction() { return eventsByAction; }
    public void setEventsByAction(Map<String, Long> eventsByAction) { this.eventsByAction = eventsByAction; }

    public Map<String, Long> getEventsByCountry() { return eventsByCountry; }
    public void setEventsByCountry(Map<String, Long> eventsByCountry) { this.eventsByCountry = eventsByCountry; }

    public Map<String, Long> getEventsByBrowser() { return eventsByBrowser; }
    public void setEventsByBrowser(Map<String, Long> eventsByBrowser) { this.eventsByBrowser = eventsByBrowser; }

    public long getL2CacheHits() { return l2CacheHits; }
    public void setL2CacheHits(long l2CacheHits) { this.l2CacheHits = l2CacheHits; }

    public long getL2CacheMisses() { return l2CacheMisses; }
    public void setL2CacheMisses(long l2CacheMisses) { this.l2CacheMisses = l2CacheMisses; }

    public long getQueryCacheHits() { return queryCacheHits; }
    public void setQueryCacheHits(long queryCacheHits) { this.queryCacheHits = queryCacheHits; }

    public long getQueryCacheMisses() { return queryCacheMisses; }
    public void setQueryCacheMisses(long queryCacheMisses) { this.queryCacheMisses = queryCacheMisses; }
}
