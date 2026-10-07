package com.boulangerie.production.specification;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.model.EtatPain;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class DestinationProductionSpecifications {

    private DestinationProductionSpecifications() {}

    public static Specification<DestinationProduction> withFilters(
            CanalDistribution canal,
            EtatPain etatPain,
            List<Long> produitIds,
            List<Long> livreurIds,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        return Specification
                .<DestinationProduction>where(SearchSpecifications.equal("canal", canal))
                .and(SearchSpecifications.equal("etatPain", etatPain))
                .and(idsIn("produitId", produitIds))
                .and(idsIn("livreurId", livreurIds))
                .and(dateBetween(dateDebut, dateFin));
    }

    public static Specification<DestinationProduction> idsIn(String field, List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return null;
            }
            return root.get(field).in(ids);
        };
    }

    public static Specification<DestinationProduction> dateBetween(LocalDate start, LocalDate end) {
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