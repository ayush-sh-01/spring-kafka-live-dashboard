package com.analytics.engine.repository;

import com.analytics.engine.model.UserAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAgentRepository extends JpaRepository<UserAgent, Long> {
    Optional<UserAgent> findByBrowserAndOsAndDeviceType(String browser, String os, String deviceType);
}
