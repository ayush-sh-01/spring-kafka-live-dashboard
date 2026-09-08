package com.analytics.engine;

import com.analytics.engine.dto.ActivityEventDto;
import com.analytics.engine.dto.AnalyticsFilterRequest;
import com.analytics.engine.dto.DetailedActivityLogDto;
import com.analytics.engine.model.ActivityLog;
import com.analytics.engine.model.Geolocation;
import com.analytics.engine.model.UserAgent;
import com.analytics.engine.repository.GeolocationRepository;
import com.analytics.engine.repository.UserAgentRepository;
import com.analytics.engine.service.ActivityPersistenceService;
import com.analytics.engine.service.AnalyticsReportingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class AnalyticsEngineIntegrationTest {

    @Autowired
    private ActivityPersistenceService persistenceService;

    @Autowired
    private AnalyticsReportingService reportingService;

    @Autowired
    private GeolocationRepository geolocationRepository;

    @Autowired
    private UserAgentRepository userAgentRepository;

    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("Verify event persistence and dynamic Criteria API filtering")
    void testEventIngestionAndCriteriaQuery() {
        ActivityEventDto event = new ActivityEventDto(
                "tester_john", "France", "Paris", "192.168.1.50",
                "Firefox", "Linux", "Desktop",
                "EXPORT", "/api/v1/export", 45L
        );

        ActivityLog saved = persistenceService.saveEvent(event);
        assertNotNull(saved.getId());

        // Test Criteria API with filters
        AnalyticsFilterRequest filter = new AnalyticsFilterRequest();
        filter.setActionType("EXPORT");
        filter.setCountry("France");

        Page<DetailedActivityLogDto> results = reportingService.getDetailedFilteredReport(filter);
        assertFalse(results.isEmpty());
        assertTrue(results.getContent().stream().anyMatch(log -> "Paris".equals(log.getCity())));
    }

    @Test
    @DisplayName("Verify Second-Level Cache (L2) operation on Geolocation")
    void testSecondLevelCacheLookup() {
        // Step 1: Persist and commit in Transaction 1 so Hibernate L2 cache receives the committed entity
        Long geoId = transactionTemplate.execute(status -> {
            Geolocation geo = new Geolocation("Spain", "Madrid", "10.0.0.1");
            return geolocationRepository.save(geo).getId();
        });
        assertNotNull(geoId);

        Session session = entityManager.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.clear();

        // Step 2: In fresh Transaction 2, load the entity.
        // It will either be loaded from L2 or put into L2
        transactionTemplate.execute(status -> {
            Geolocation firstLoad = entityManager.find(Geolocation.class, geoId);
            assertNotNull(firstLoad);
            assertEquals("Madrid", firstLoad.getCity());
            return null;
        });

        // Step 3: In fresh Transaction 3, load again.
        // The first-level cache (EntityManager) is completely fresh, so it MUST hit the L2 cache!
        transactionTemplate.execute(status -> {
            Geolocation secondLoad = entityManager.find(Geolocation.class, geoId);
            assertNotNull(secondLoad);
            assertEquals("Madrid", secondLoad.getCity());
            return null;
        });

        long hitCount = stats.getSecondLevelCacheHitCount();
        assertTrue(hitCount >= 1,
                "Expected at least 1 L2 Cache hit for Geolocation entity, actual hits: " + hitCount);
    }
}
