package com.boulangerie.shared.repository;

import com.boulangerie.shared.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> entityName(String entityName) {

        return (root, query, cb) ->
                entityName == null
                        ? null
                        : cb.equal(root.get("entityName"), entityName);
    }

    public static Specification<AuditLog> entityId(Long entityId) {

        return (root, query, cb) ->
                entityId == null
                        ? null
                        : cb.equal(root.get("entityId"), entityId);
    }

    public static Specification<AuditLog> action(AuditAction action) {

        return (root, query, cb) ->
                action == null
                        ? null
                        : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> performedBy(String performedBy) {

        return (root, query, cb) ->
                performedBy == null
                        ? null
                        : cb.equal(root.get("performedBy"), performedBy);
    }

    public static Specification<AuditLog> performedAtBetween(
            Instant debut,
            Instant fin
    ) {

        return (root, query, cb) -> {

            if (debut == null && fin == null) {
                return null;
            }

            if (debut == null) {
                return cb.lessThanOrEqualTo(root.get("performedAt"), fin);
            }

            if (fin == null) {
                return cb.greaterThanOrEqualTo(
                        root.get("performedAt"),
                        debut
                );
            }

            return cb.between(root.get("performedAt"), debut, fin);
        };
    }
}