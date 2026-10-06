package com.boulangerie.shared.specification;

import org.springframework.data.jpa.domain.Specification;

/**
 * Centralized specifications for autocomplete and search operations.
 * Follows the DRY principle and provides reusable search predicates.
 */
public class SearchSpecifications {

    private SearchSpecifications() {}

    /**
     * Search by a single text field (case-insensitive LIKE match).
     */
    public static <T> Specification<T> searchByField(String field, String query) {
        return (root, queryContext, cb) -> {
            if (query == null || query.isBlank()) return null;
            return cb.like(cb.lower(root.get(field)), "%" + query.toLowerCase() + "%");
        };
    }

    /**
     * Search by multiple fields with OR logic (case-insensitive LIKE) - 2 fields.
     */
    public static <T> Specification<T> searchByMultipleFields(String query, String field1, String field2) {
        return (root, queryContext, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }
            String lowerQuery = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get(field1)), lowerQuery),
                    cb.like(cb.lower(root.get(field2)), lowerQuery)
            );
        };
    }

    /**
     * Search by multiple fields with OR logic (case-insensitive LIKE) - 3 fields.
     */
    public static <T> Specification<T> searchByMultipleFields(String query, String field1, String field2, String field3) {
        return (root, queryContext, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }
            String lowerQuery = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get(field1)), lowerQuery),
                    cb.like(cb.lower(root.get(field2)), lowerQuery),
                    cb.like(cb.lower(root.get(field3)), lowerQuery)
            );
        };
    }

    /**
     * Filter by active status (actif = true).
     */
    public static <T> Specification<T> isActive() {
        return (root, queryContext, cb) -> cb.isTrue(root.get("actif"));
    }

    /**
     * Combine search and active filter - single field.
     */
    public static <T> Specification<T> activeAndSearchByField(String field, String query) {
        Specification<T> active = isActive();
        Specification<T> search = searchByField(field, query);
        return Specification.where(active).and(search);
    }

    /**
     * Combine search (2 fields) and active filter.
     */
    public static <T> Specification<T> activeAndSearchByMultipleFields(String query, String field1, String field2) {
        Specification<T> active = isActive();
        Specification<T> search = searchByMultipleFields(query, field1, field2);
        return Specification.where(active).and(search);
    }

    /**
     * Combine search (3 fields) and active filter.
     */
    public static <T> Specification<T> activeAndSearchByMultipleFields(String query, String field1, String field2, String field3) {
        Specification<T> active = isActive();
        Specification<T> search = searchByMultipleFields(query, field1, field2, field3);
        return Specification.where(active).and(search);
    }
}
