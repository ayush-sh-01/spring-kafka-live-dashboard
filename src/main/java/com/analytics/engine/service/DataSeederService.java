package com.analytics.engine.service;

import com.analytics.engine.dto.ActivityEventDto;
import com.analytics.engine.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class DataSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final ActivityPersistenceService persistenceService;
    private final ActivityLogRepository activityLogRepository;

    public DataSeederService(ActivityPersistenceService persistenceService,
                             ActivityLogRepository activityLogRepository) {
        this.persistenceService = persistenceService;
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    public void run(String... args) {
        if (activityLogRepository.count() > 0) {
            log.info("Database already contains activity data. Skipping initial seeding.");
            return;
        }

        log.info("Seeding initial analytics data with users, lookup dimensions, and activity logs...");

        String[] users = {"alice_wonder", "bob_builder", "charlie_brown", "diana_prince", "ethan_hunt"};
        String[][] locations = {
                {"United States", "New York", "198.51.100.1"},
                {"Germany", "Berlin", "198.51.100.2"},
                {"India", "Bengaluru", "198.51.100.3"},
                {"United Kingdom", "London", "198.51.100.4"},
                {"Japan", "Tokyo", "198.51.100.5"}
        };
        String[][] devices = {
                {"Chrome", "Windows 11", "Desktop"},
                {"Safari", "macOS Sonoma", "Desktop"},
                {"Firefox", "Ubuntu Linux", "Desktop"},
                {"Chrome Mobile", "Android 14", "Mobile"},
                {"Mobile Safari", "iOS 17", "Mobile"}
        };
        String[] actions = {"PAGE_VIEW", "PURCHASE", "LOGIN", "EXPORT", "SEARCH", "CHECKOUT"};
        String[] endpoints = {"/dashboard", "/products/101", "/api/checkout", "/home", "/export/pdf", "/search?q=laptop"};

        Random random = new Random();

        for (int i = 0; i < 35; i++) {
            String user = users[random.nextInt(users.length)];
            String[] loc = locations[random.nextInt(locations.length)];
            String[] dev = devices[random.nextInt(devices.length)];
            String action = actions[random.nextInt(actions.length)];
            String endpoint = endpoints[random.nextInt(endpoints.length)];
            long responseTime = 20 + random.nextInt(180);

            ActivityEventDto event = new ActivityEventDto(
                    user, loc[0], loc[1], loc[2],
                    dev[0], dev[1], dev[2],
                    action, endpoint, responseTime
            );

            persistenceService.saveEvent(event);
        }

        log.info("Initial data seeding completed successfully with 35 activity logs.");
    }
}
