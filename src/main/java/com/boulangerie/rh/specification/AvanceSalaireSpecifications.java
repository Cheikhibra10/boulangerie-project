package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.AvanceSalaireFilter;
import com.boulangerie.rh.model.AvanceSalaire;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class AvanceSalaireSpecifications {

    private AvanceSalaireSpecifications() {}

    public static Specification<AvanceSalaire> withFilters(
            AvanceSalaireFilter filter,
            List<Long> employeIds
    ) {
        return Specification
                .<AvanceSalaire>where(SearchSpecifications.equal("statut", filter.statut()))
                .and(employeIdsIn(employeIds))
                .and(dateDemandeBetween(filter.dateDebut(), filter.dateFin()));
    }

    public static Specification<AvanceSalaire> employeIdsIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return null;
            }
            return root.get("employe").get("id").in(ids);
        };
    }

    public static Specification<AvanceSalaire> dateDemandeBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }
            if (start != null && end != null) {
                return cb.between(root.get("dateDemande"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("dateDemande"), start);
            }
            return cb.lessThanOrEqualTo(root.get("dateDemande"), end);
        };
    }
}