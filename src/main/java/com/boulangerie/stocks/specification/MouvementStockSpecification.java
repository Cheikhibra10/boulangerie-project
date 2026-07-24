// stocks/specification/MouvementStockSpecification.java
package com.boulangerie.stocks.specification;

import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class MouvementStockSpecification {

    private MouvementStockSpecification() {}

    public static Specification<MouvementStock> byIngredient(Long ingredientId) {
        return (root, query, cb) -> cb.equal(root.get("ingredient").get("id"), ingredientId);
    }

    public static Specification<MouvementStock> byType(TypeMouvementStock type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<MouvementStock> dateBetween(Instant start, Instant end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start == null) return cb.lessThanOrEqualTo(root.get("date"), end);
            if (end == null) return cb.greaterThanOrEqualTo(root.get("date"), start);
            return cb.between(root.get("date"), start, end);
        };
    }

    public static Specification<MouvementStock> byStatut(StatutMouvement statut) {
        return (root, query, cb) -> statut == null ? cb.conjunction() : cb.equal(root.get("statut"), statut);
    }
}