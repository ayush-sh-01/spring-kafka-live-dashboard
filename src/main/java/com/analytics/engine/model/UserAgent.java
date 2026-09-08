package com.analytics.engine.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.io.Serializable;
import java.util.Objects;

/**
 * UserAgent / DeviceProfile lookup entity.
 * Second-Level Cache (L2) is enabled: browser, OS, and device types are static lookup data.
 */
@Entity
@Table(name = "user_agents")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserAgent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String browser;

    @Column(nullable = false, length = 64)
    private String os;

    @Column(name = "device_type", nullable = false, length = 32)
    private String deviceType;

    public UserAgent() {}

    public UserAgent(String browser, String os, String deviceType) {
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAgent userAgent = (UserAgent) o;
        return Objects.equals(id, userAgent.id) ||
               (Objects.equals(browser, userAgent.browser) &&
                Objects.equals(os, userAgent.os) &&
                Objects.equals(deviceType, userAgent.deviceType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, browser, os, deviceType);
    }
}
