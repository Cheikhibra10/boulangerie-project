package com.boulangerie.shared.specification;

import org.springframework.data.jpa.domain.Specification;
import java.util.Collection;

public final class Specs {

    private Specs() {}

    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, cb) ->
                value == null ? null : cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return null;
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    public static <T> Specification<T> isTrue(String field) {
        return (root, query, cb) -> cb.isTrue(root.get(field));
    }

    public static <T> Specification<T> isFalse(String field) {
        return (root, query, cb) -> cb.isFalse(root.get(field));
    }
}