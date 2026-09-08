package com.analytics.engine.dto;

import java.io.Serializable;

public class ActivityEventDto implements Serializable {
    private String username;
    private String country;
    private String city;
    private String ipAddress;
    private String browser;
    private String os;
    private String deviceType;
    private String actionType;
    private String endpoint;
    private Long responseTimeMs;

    public ActivityEventDto() {}

    public ActivityEventDto(String username, String country, String city, String ipAddress,
                            String browser, String os, String deviceType,
                            String actionType, String endpoint, Long responseTimeMs) {
        this.username = username;
        this.country = country;
        this.city = city;
        this.ipAddress = ipAddress;
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
        this.actionType = actionType;
        this.endpoint = endpoint;
        this.responseTimeMs = responseTimeMs;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

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
}
