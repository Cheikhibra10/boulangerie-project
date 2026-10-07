package com.boulangerie.production.specification;

import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.model.StatutProduction;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class LotProductionSpecifications {

    private LotProductionSpecifications() {}

    public static Specification<LotProduction> withFilters(
            List<Long> produitIds,
            StatutProduction statut,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        return Specification
                .<LotProduction>where(produitIdsIn(produitIds))
                .and(SearchSpecifications.equal("statut", statut))
                .and(dateBetween(dateDebut, dateFin));
    }

    public static Specification<LotProduction> produitIdsIn(List<Long> produitIds) {
        return (root, query, cb) -> {
            if (produitIds == null || produitIds.isEmpty()) {
                return null;
            }
            return root.get("produitId").in(produitIds);
        };
    }

    public static Specification<LotProduction> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }
            if (start != null && end != null) {
                return cb.between(root.get("date"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), start);
            }
            return cb.lessThanOrEqualTo(root.get("date"), end);
        };
    }
}