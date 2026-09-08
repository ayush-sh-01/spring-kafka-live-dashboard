package com.analytics.engine.repository;

import com.analytics.engine.dto.AnalyticsFilterRequest;
import com.analytics.engine.model.ActivityLog;
import org.springframework.data.domain.Page;

public interface ActivityLogCriteriaRepository {
    Page<ActivityLog> findByDynamicFilters(AnalyticsFilterRequest filter);
}
