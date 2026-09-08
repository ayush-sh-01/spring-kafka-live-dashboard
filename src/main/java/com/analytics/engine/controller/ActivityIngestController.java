package com.analytics.engine.controller;

import com.analytics.engine.dto.ActivityEventDto;
import com.analytics.engine.service.ActivityProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/events")
public class ActivityIngestController {

    private final ActivityProducerService producerService;

    public ActivityIngestController(ActivityProducerService producerService) {
        this.producerService = producerService;
    }

    /**
     * Ingests an activity event asynchronously via Kafka.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestEvent(@RequestBody ActivityEventDto event) {
        producerService.publishEvent(event);
        return ResponseEntity.ok(Map.of(
                "status", "QUEUED",
                "message", "Activity event pushed to Kafka topic 'user-activity-events'"
        ));
    }

    /**
     * Utility endpoint to simulate high-frequency clickstream traffic.
     * Generates 'count' random events to test Kafka streaming, caching, and WebSocket live updates.
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateClicks(@RequestParam(defaultValue = "10") int count) {
        String[] users = {"alice_wonder", "bob_builder", "charlie_brown", "diana_prince", "ethan_hunt", "fiona_gallagher"};
        String[][] locations = {
                {"United States", "New York", "198.51.100.10"},
                {"Germany", "Berlin", "198.51.100.20"},
                {"India", "Bengaluru", "198.51.100.30"},
                {"United Kingdom", "London", "198.51.100.40"},
                {"Japan", "Tokyo", "198.51.100.50"},
                {"Canada", "Toronto", "198.51.100.60"}
        };
        String[][] devices = {
                {"Chrome", "Windows 11", "Desktop"},
                {"Safari", "macOS Sonoma", "Desktop"},
                {"Firefox", "Ubuntu Linux", "Desktop"},
                {"Chrome Mobile", "Android 14", "Mobile"},
                {"Mobile Safari", "iOS 17", "Mobile"}
        };
        String[] actions = {"PAGE_VIEW", "PURCHASE", "LOGIN", "EXPORT", "SEARCH", "CHECKOUT"};
        String[] endpoints = {"/dashboard", "/products/200", "/api/checkout", "/home", "/export/csv", "/cart"};

        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String user = users[random.nextInt(users.length)];
            String[] loc = locations[random.nextInt(locations.length)];
            String[] dev = devices[random.nextInt(devices.length)];
            String action = actions[random.nextInt(actions.length)];
            String endpoint = endpoints[random.nextInt(endpoints.length)];
            long responseTime = 15 + random.nextInt(150);

            ActivityEventDto event = new ActivityEventDto(
                    user, loc[0], loc[1], loc[2],
                    dev[0], dev[1], dev[2],
                    action, endpoint, responseTime
            );

            producerService.publishEvent(event);
        }

        return ResponseEntity.ok(Map.of(
                "status", "SIMULATED",
                "count", count,
                "message", "Successfully streamed " + count + " events"
        ));
    }
}
