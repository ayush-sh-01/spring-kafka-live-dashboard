package com.analytics.engine.service;

import com.analytics.engine.dto.ActivityEventDto;
import com.analytics.engine.model.ActivityLog;
import com.analytics.engine.model.Geolocation;
import com.analytics.engine.model.User;
import com.analytics.engine.model.UserAgent;
import com.analytics.engine.repository.ActivityLogRepository;
import com.analytics.engine.repository.GeolocationRepository;
import com.analytics.engine.repository.UserAgentRepository;
import com.analytics.engine.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists activity events into PostgreSQL / DB.
 * 
 * INTERVIEW SOLUTION:
 * Writes are routed through Hibernate's persistence context.
 * When ActivityLog is saved via JPA, Hibernate automatically updates its
 * internal update-timestamps-region, ensuring that stale query cache entries
 * are invalidated in synchronization with database changes.
 */
@Service
public class ActivityPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ActivityPersistenceService.class);

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final GeolocationRepository geolocationRepository;
    private final UserAgentRepository userAgentRepository;

    public ActivityPersistenceService(ActivityLogRepository activityLogRepository,
                                      UserRepository userRepository,
                                      GeolocationRepository geolocationRepository,
                                      UserAgentRepository userAgentRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.geolocationRepository = geolocationRepository;
        this.userAgentRepository = userAgentRepository;
    }

    @Transactional
    public ActivityLog saveEvent(ActivityEventDto dto) {
        // 1. Resolve or create User (Cached across application)
        String username = (dto.getUsername() != null && !dto.getUsername().isEmpty()) 
                ? dto.getUsername() : "anonymous_user";
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(
                        username,
                        username + "@analytics.local",
                        "STANDARD",
                        "Engineering"
                )));

        // 2. Resolve or create Geolocation (L2 Cached lookup table)
        String country = (dto.getCountry() != null && !dto.getCountry().isEmpty()) ? dto.getCountry() : "Unknown";
        String city = (dto.getCity() != null && !dto.getCity().isEmpty()) ? dto.getCity() : "Unknown";
        Geolocation geolocation = geolocationRepository.findByCountryAndCity(country, city)
                .orElseGet(() -> geolocationRepository.save(new Geolocation(
                        country,
                        city,
                        dto.getIpAddress() != null ? dto.getIpAddress() : "127.0.0.1"
                )));

        // 3. Resolve or create UserAgent (L2 Cached lookup table)
        String browser = (dto.getBrowser() != null && !dto.getBrowser().isEmpty()) ? dto.getBrowser() : "Chrome";
        String os = (dto.getOs() != null && !dto.getOs().isEmpty()) ? dto.getOs() : "Windows";
        String deviceType = (dto.getDeviceType() != null && !dto.getDeviceType().isEmpty()) ? dto.getDeviceType() : "Desktop";
        UserAgent userAgent = userAgentRepository.findByBrowserAndOsAndDeviceType(browser, os, deviceType)
                .orElseGet(() -> userAgentRepository.save(new UserAgent(browser, os, deviceType)));

        // 4. Create and persist ActivityLog
        ActivityLog activityLog = new ActivityLog(
                user,
                geolocation,
                userAgent,
                dto.getActionType() != null ? dto.getActionType() : "PAGE_VIEW",
                dto.getEndpoint() != null ? dto.getEndpoint() : "/home",
                dto.getResponseTimeMs() != null ? dto.getResponseTimeMs() : (long) (Math.random() * 200 + 20),
                LocalDateTime.now()
        );

        return activityLogRepository.save(activityLog);
    }
}
