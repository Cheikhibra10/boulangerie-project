package com.boulangerie.shared.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public final class SearchSpecifications {

    private SearchSpecifications() {}

    private static <T> Path<?> resolvePath(Root<T> root, String field) {
        Path<?> path = root;
        for (String part : field.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }

    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, cb) ->
                value == null ? null : cb.equal(resolvePath(root, field), value);
    }

    public static <T> Specification<T> like(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank()) return null;
            return cb.like(
                    cb.lower(resolvePath(root, field).as(String.class)),
                    "%" + value.toLowerCase().trim() + "%"
            );
        };
    }

    public static <T> Specification<T> likeAny(String value, String... fields) {
        return (root, query, cb) -> {
            if (value == null || value.isBlank() || fields == null || fields.length == 0) {
                return null;
            }
            String pattern = "%" + value.toLowerCase().trim() + "%";
            var predicates = java.util.Arrays.stream(fields)
                    .map(f -> cb.like(cb.lower(resolvePath(root, f).as(String.class)), pattern))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.or(predicates);
        };
    }

    public static <T> Specification<T> isTrue(String field) {
        return (root, query, cb) -> cb.isTrue(resolvePath(root, field).as(Boolean.class));
    }

    public static <T> Specification<T> isFalse(String field) {
        return (root, query, cb) -> cb.isFalse(resolvePath(root, field).as(Boolean.class));
    }

    public static <T> Specification<T> isActive() {
        return isTrue("actif");
    }
}