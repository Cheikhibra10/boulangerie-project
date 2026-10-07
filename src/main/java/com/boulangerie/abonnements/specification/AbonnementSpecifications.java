package com.boulangerie.abonnements.specification;

import com.boulangerie.abonnements.dto.AbonnementFilter;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class AbonnementSpecifications {

    private AbonnementSpecifications() {}

    public static Specification<Abonnement> withFilters(AbonnementFilter filter, List<Long> livreurIds) {
        return Specification
                .<Abonnement>where(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"))
                .and(livreurIdsIn(livreurIds));
    }

    public static Specification<Abonnement> livreurIdsIn(List<Long> livreurIds) {
        return (root, query, cb) -> {
            if (livreurIds == null || livreurIds.isEmpty()) {
                return null;
            }
            return root.get("livreurId").in(livreurIds);
        };
    }

    public static Specification<Abonnement> isActive() {
        return SearchSpecifications.isTrue("actif");
    }
}