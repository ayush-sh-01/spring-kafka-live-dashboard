package com.analytics.engine.repository;

import com.analytics.engine.dto.AnalyticsFilterRequest;
import com.analytics.engine.model.ActivityLog;
import com.analytics.engine.model.Geolocation;
import com.analytics.engine.model.User;
import com.analytics.engine.model.UserAgent;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Criteria API Implementation.
 * 
 * Constructing dynamic queries programmatically based on user-selected search filters:
 * - Date ranges
 * - User demographics / username
 * - Action types
 * - Country / Geolocation
 * - Browser / UserAgent
 * 
 * Uses @NamedEntityGraph hint to eagerly load associated dimensions without N+1 queries.
 */
@Repository
public class ActivityLogCriteriaRepositoryImpl implements ActivityLogCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ActivityLog> findByDynamicFilters(AnalyticsFilterRequest filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // 1. Build main query
        CriteriaQuery<ActivityLog> query = cb.createQuery(ActivityLog.class);
        Root<ActivityLog> root = query.from(ActivityLog.class);

        // Build list of dynamic predicates (WHERE clauses)
        List<Predicate> predicates = buildPredicates(filter, cb, root);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<ActivityLog> typedQuery = entityManager.createQuery(query);

        // Apply Entity Graph hint: fetch User, Geolocation, and UserAgent in a single JOIN
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("ActivityLog.detailedReport");
        typedQuery.setHint("jakarta.persistence.fetchgraph", entityGraph);

        // Optional Query Cache hint
        typedQuery.setHint("org.hibernate.cacheable", true);

        // Pagination
        int page = Math.max(0, filter.getPage());
        int size = filter.getSize() > 0 ? filter.getSize() : 20;
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<ActivityLog> content = typedQuery.getResultList();

        // 2. Count query for total elements
        long total = countByDynamicFilters(filter);

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    private long countByDynamicFilters(AnalyticsFilterRequest filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ActivityLog> root = countQuery.from(ActivityLog.class);

        countQuery.select(cb.count(root));

        List<Predicate> predicates = buildPredicates(filter, cb, root);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);
        typedCountQuery.setHint("org.hibernate.cacheable", true);
        return typedCountQuery.getSingleResult();
    }

    private List<Predicate> buildPredicates(AnalyticsFilterRequest filter, CriteriaBuilder cb, Root<ActivityLog> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getActionType() != null && !filter.getActionType().trim().isEmpty()) {
            predicates.add(cb.equal(root.get("actionType"), filter.getActionType().trim()));
        }

        if (filter.getMinResponseTime() != null && filter.getMinResponseTime() > 0) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("responseTimeMs"), filter.getMinResponseTime()));
        }

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            predicates.add(cb.between(root.get("createdAt"), filter.getStartDate(), filter.getEndDate()));
        } else if (filter.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
        } else if (filter.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
        }

        // Join filters for related entities
        if (filter.getCountry() != null && !filter.getCountry().trim().isEmpty()) {
            Join<ActivityLog, Geolocation> geoJoin = root.join("geolocation", JoinType.LEFT);
            predicates.add(cb.equal(geoJoin.get("country"), filter.getCountry().trim()));
        }

        if (filter.getBrowser() != null && !filter.getBrowser().trim().isEmpty()) {
            Join<ActivityLog, UserAgent> uaJoin = root.join("userAgent", JoinType.LEFT);
            predicates.add(cb.equal(uaJoin.get("browser"), filter.getBrowser().trim()));
        }

        if (filter.getUsername() != null && !filter.getUsername().trim().isEmpty()) {
            Join<ActivityLog, User> userJoin = root.join("user", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(userJoin.get("username")), "%" + filter.getUsername().trim().toLowerCase() + "%"));
        }

        return predicates;
    }
}
