package com.analytics.engine.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.io.Serializable;
import java.util.Objects;

/**
 * Geolocation lookup entity.
 * Second-Level Cache (L2) is enabled: countries and cities are static/infrequently updated,
 * so caching them avoids repetitive database roundtrips.
 */
@Entity
@Table(name = "geolocations")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Geolocation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String country;

    @Column(nullable = false, length = 64)
    private String city;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    public Geolocation() {}

    public Geolocation(String country, String city, String ipAddress) {
        this.country = country;
        this.city = city;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Geolocation that = (Geolocation) o;
        return Objects.equals(id, that.id) || 
               (Objects.equals(country, that.country) && Objects.equals(city, that.city));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, country, city);
    }
}
