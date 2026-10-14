package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.ContratFilter;
import com.boulangerie.rh.model.Contrat;
import com.boulangerie.rh.model.StatutContrat;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class ContratSpecifications {

    private ContratSpecifications() {}

    public static Specification<Contrat> withFilters(ContratFilter filter, List<Long> employeIds) {
        return Specification
                .<Contrat>where(SearchSpecifications.equal("type", filter.type()))
                .and(SearchSpecifications.equal("statut", filter.statut()))
                .and(employeIdsIn(employeIds));
    }

    public static Specification<Contrat> employeIdsIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) return null;
            return root.get("employe").get("id").in(ids);
        };
    }

    public static Specification<Contrat> actifForEmploye(Long employeId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("employe").get("id"), employeId),
                cb.equal(root.get("statut"), StatutContrat.ACTIF)
        );
    }
}

