package com.analytics.engine.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * High-volume transactional entity capturing clickstream and user events.
 * 
 * Uses @NamedEntityGraph to solve the N+1 problem:
 * When generating detailed reports, all related dimensions (User, Geolocation, UserAgent)
 * are fetched in ONE single SQL JOIN query instead of N separate queries!
 */
@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_activity_action_type", columnList = "action_type"),
    @Index(name = "idx_activity_created_at", columnList = "created_at")
})
@NamedEntityGraph(
    name = "ActivityLog.detailedReport",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("geolocation"),
        @NamedAttributeNode("userAgent")
    }
)
public class ActivityLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FetchType.LAZY avoids loading these associations unnecessarily during simple queries
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geolocation_id", nullable = false)
    private Geolocation geolocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_agent_id", nullable = false)
    private UserAgent userAgent;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType; // e.g., "PAGE_VIEW", "PURCHASE", "LOGIN", "EXPORT", "CLICK"

    @Column(nullable = false, length = 255)
    private String endpoint;   // e.g., "/checkout", "/products/42", "/dashboard"

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(User user, Geolocation geolocation, UserAgent userAgent, 
                       String actionType, String endpoint, Long responseTimeMs, LocalDateTime createdAt) {
        this.user = user;
        this.geolocation = geolocation;
        this.userAgent = userAgent;
        this.actionType = actionType;
        this.endpoint = endpoint;
        this.responseTimeMs = responseTimeMs;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Geolocation getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Geolocation geolocation) {
        this.geolocation = geolocation;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(UserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
