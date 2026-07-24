// caisse/specification/MouvementSpecification.java
package com.boulangerie.comptabilite.specification;

import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.comptabilite.model.SensMouvement;
import com.boulangerie.comptabilite.model.TypeMouvement;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class MouvementCaisseSpecification {

    private MouvementCaisseSpecification() {}

    public static Specification<MouvementCaisse> byCaisse(Long caisseId) {
        return (root, query, cb) -> cb.equal(root.get("caisse").get("id"), caisseId);
    }

    public static Specification<MouvementCaisse> dateBetween(Instant start, Instant end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return cb.conjunction();
            if (start == null) return cb.lessThanOrEqualTo(root.get("date"), end);
            if (end == null) return cb.greaterThanOrEqualTo(root.get("date"), start);
            return cb.between(root.get("date"), start, end);
        };
    }

    public static Specification<MouvementCaisse> byType(TypeMouvement type) {
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("typeMouvement"), type);
    }

    public static Specification<MouvementCaisse> bySens(SensMouvement sens) {
        return (root, query, cb) -> sens == null ? cb.conjunction() : cb.equal(root.get("sens"), sens);
    }

    public static Specification<MouvementCaisse> byCategorieDepense(Long categorieId) {
        return (root, query, cb) -> {
            if (categorieId == null) return cb.conjunction();
            // On suppose qu'il y a une relation via depensePeriode
            // Ici on utilise une jointure si nécessaire
            return cb.equal(root.get("depensePeriode").get("categorie").get("id"), categorieId);
        };
    }

    public static Specification<MouvementCaisse> byLivreur(Long livreurId) {
        return (root, query, cb) -> {
            if (livreurId == null) return cb.conjunction();
            return cb.equal(root.get("livreur").get("id"), livreurId);
        };
    }
}