package com.analytics.engine.service;

import com.analytics.engine.dto.AnalyticsSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled Aggregator Service.
 * Periodically aggregates analytics metrics and broadcasts updates in real-time
 * to all connected browser clients via WebSocket STOMP broker.
 */
@Service
@EnableScheduling
public class RealtimeAggregationScheduler {

    private static final Logger log = LoggerFactory.getLogger(RealtimeAggregationScheduler.class);

    private final AnalyticsReportingService reportingService;
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeAggregationScheduler(AnalyticsReportingService reportingService,
                                       SimpMessagingTemplate messagingTemplate) {
        this.reportingService = reportingService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcasts real-time aggregated metrics every 3 seconds to active WebSocket subscribers.
     */
    @Scheduled(fixedRate = 3000)
    public void pushRealtimeAnalytics() {
        try {
            AnalyticsSummaryDto summary = reportingService.getLiveSummary();
            messagingTemplate.convertAndSend("/topic/analytics", summary);
        } catch (Exception e) {
            log.debug("WebSocket broadcast skipped (no active transactions or DB initializing): {}", e.getMessage());
        }
    }
}
