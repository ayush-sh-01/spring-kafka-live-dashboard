package com.analytics.engine.dto;

import java.time.LocalDateTime;

/**
 * Filter criteria for dynamic reporting via JPA Criteria API.
 */
public class AnalyticsFilterRequest {
    private String actionType;
    private String country;
    private String browser;
    private String username;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long minResponseTime;
    private int page = 0;
    private int size = 20;

    public AnalyticsFilterRequest() {}

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Long getMinResponseTime() { return minResponseTime; }
    public void setMinResponseTime(Long minResponseTime) { this.minResponseTime = minResponseTime; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
