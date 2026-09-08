package com.analytics.engine.controller;

import com.analytics.engine.dto.AnalyticsFilterRequest;
import com.analytics.engine.dto.AnalyticsSummaryDto;
import com.analytics.engine.dto.DetailedActivityLogDto;
import com.analytics.engine.service.AnalyticsReportingService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class AnalyticsReportController {

    private final AnalyticsReportingService reportingService;

    public AnalyticsReportController(AnalyticsReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * Returns high-level reporting aggregates.
     * Uses Hibernate Query Cache to avoid repeated expensive SQL aggregations.
     */
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        return ResponseEntity.ok(reportingService.getLiveSummary());
    }

    /**
     * Dynamic multi-filter searching powered by JPA Criteria API
     * and eager fetching via @NamedEntityGraph.
     */
    @PostMapping("/filter")
    public ResponseEntity<Page<DetailedActivityLogDto>> getFilteredLogs(@RequestBody AnalyticsFilterRequest filter) {
        return ResponseEntity.ok(reportingService.getDetailedFilteredReport(filter));
    }

    /**
     * Fetches top 50 recent detailed activity logs (User + Geolocation + UserAgent).
     */
    @GetMapping("/recent")
    public ResponseEntity<List<DetailedActivityLogDto>> getRecentLogs() {
        return ResponseEntity.ok(reportingService.getRecentDetailedLogs());
    }
}
