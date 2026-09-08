package com.analytics.engine.dto;

import java.time.LocalDateTime;

public class DetailedActivityLogDto {
    private Long id;
    private String username;
    private String userEmail;
    private String country;
    private String city;
    private String browser;
    private String os;
    private String deviceType;
    private String actionType;
    private String endpoint;
    private Long responseTimeMs;
    private LocalDateTime createdAt;

    public DetailedActivityLogDto() {}

    public DetailedActivityLogDto(Long id, String username, String userEmail, 
                                  String country, String city, String browser, 
                                  String os, String deviceType, String actionType, 
                                  String endpoint, Long responseTimeMs, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.userEmail = userEmail;
        this.country = country;
        this.city = city;
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
        this.actionType = actionType;
        this.endpoint = endpoint;
        this.responseTimeMs = responseTimeMs;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
