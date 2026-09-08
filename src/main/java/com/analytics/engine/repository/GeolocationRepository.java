package com.analytics.engine.repository;

import com.analytics.engine.model.Geolocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeolocationRepository extends JpaRepository<Geolocation, Long> {
    Optional<Geolocation> findByCountryAndCity(String country, String city);
}
