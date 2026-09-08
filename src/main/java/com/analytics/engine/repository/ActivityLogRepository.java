package com.analytics.engine.repository;

import com.analytics.engine.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, ActivityLogCriteriaRepository {

    /**
     * Eagerly loads User, Geolocation, and UserAgent in a SINGLE SQL JOIN
     * using the defined @NamedEntityGraph, completely avoiding the N+1 query problem.
     */
    @EntityGraph(value = "ActivityLog.detailedReport", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findTop50DetailedLogs();

    /**
     * Demonstrates Query Cache usage on aggregate queries.
     * The org.hibernate.cacheable hint tells Hibernate to store the aggregate result
     * in the Query Cache with the configured 5-minute TTL.
     */
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT a.actionType, COUNT(a) FROM ActivityLog a GROUP BY a.actionType")
    List<Object[]> getActionTypeCountsCached();

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT g.country, COUNT(a) FROM ActivityLog a JOIN a.geolocation g GROUP BY g.country")
    List<Object[]> getCountryCountsCached();

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT u.browser, COUNT(a) FROM ActivityLog a JOIN a.userAgent u GROUP BY u.browser")
    List<Object[]> getBrowserCountsCached();

    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT AVG(a.responseTimeMs) FROM ActivityLog a")
    Double getAverageResponseTimeCached();
}
