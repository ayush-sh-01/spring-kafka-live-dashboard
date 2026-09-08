package com.analytics.engine.service;

import com.analytics.engine.dto.AnalyticsFilterRequest;
import com.analytics.engine.dto.AnalyticsSummaryDto;
import com.analytics.engine.dto.DetailedActivityLogDto;
import com.analytics.engine.model.ActivityLog;
import com.analytics.engine.repository.ActivityLogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsReportingService {

    private final ActivityLogRepository activityLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalyticsReportingService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Aggregates metrics using Hibernate Query Cache.
     * When identical aggregate parameters are requested within the TTL window,
     * results are fetched directly from cache memory.
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getLiveSummary() {
        long totalEvents = activityLogRepository.count();
        Double avgResponse = activityLogRepository.getAverageResponseTimeCached();
        double avgResponseTime = avgResponse != null ? Math.round(avgResponse * 100.0) / 100.0 : 0.0;

        // Group counts by action type (cached query)
        Map<String, Long> actionsMap = new LinkedHashMap<>();
        for (Object[] row : activityLogRepository.getActionTypeCountsCached()) {
            actionsMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        // Group counts by country (cached query)
        Map<String, Long> countriesMap = new LinkedHashMap<>();
        for (Object[] row : activityLogRepository.getCountryCountsCached()) {
            countriesMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        // Group counts by browser (cached query)
        Map<String, Long> browsersMap = new LinkedHashMap<>();
        for (Object[] row : activityLogRepository.getBrowserCountsCached()) {
            browsersMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        // Extract Hibernate cache statistics
        long l2Hits = 0, l2Misses = 0, qHits = 0, qMisses = 0;
        try {
            Session session = entityManager.unwrap(Session.class);
            Statistics stats = session.getSessionFactory().getStatistics();
            l2Hits = stats.getSecondLevelCacheHitCount();
            l2Misses = stats.getSecondLevelCacheMissCount();
            qHits = stats.getQueryCacheHitCount();
            qMisses = stats.getQueryCacheMissCount();
        } catch (Exception ignored) {}

        return new AnalyticsSummaryDto(
                totalEvents,
                avgResponseTime,
                actionsMap,
                countriesMap,
                browsersMap,
                l2Hits,
                l2Misses,
                qHits,
                qMisses
        );
    }

    /**
     * Executes dynamic search using JPA Criteria API and eagerly loads
     * associated User, Geolocation, and UserAgent via @NamedEntityGraph.
     */
    @Transactional(readOnly = true)
    public Page<DetailedActivityLogDto> getDetailedFilteredReport(AnalyticsFilterRequest filter) {
        Page<ActivityLog> page = activityLogRepository.findByDynamicFilters(filter);
        return page.map(this::mapToDetailedDto);
    }

    /**
     * Fetches recent 50 detailed logs with single-join Entity Graph.
     */
    @Transactional(readOnly = true)
    public List<DetailedActivityLogDto> getRecentDetailedLogs() {
        return activityLogRepository.findTop50DetailedLogs().stream()
                .map(this::mapToDetailedDto)
                .collect(Collectors.toList());
    }

    private DetailedActivityLogDto mapToDetailedDto(ActivityLog log) {
        return new DetailedActivityLogDto(
                log.getId(),
                log.getUser() != null ? log.getUser().getUsername() : "N/A",
                log.getUser() != null ? log.getUser().getEmail() : "N/A",
                log.getGeolocation() != null ? log.getGeolocation().getCountry() : "N/A",
                log.getGeolocation() != null ? log.getGeolocation().getCity() : "N/A",
                log.getUserAgent() != null ? log.getUserAgent().getBrowser() : "N/A",
                log.getUserAgent() != null ? log.getUserAgent().getOs() : "N/A",
                log.getUserAgent() != null ? log.getUserAgent().getDeviceType() : "N/A",
                log.getActionType(),
                log.getEndpoint(),
                log.getResponseTimeMs(),
                log.getCreatedAt()
        );
    }
}
