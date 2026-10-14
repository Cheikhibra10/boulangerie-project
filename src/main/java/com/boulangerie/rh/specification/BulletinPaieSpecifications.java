package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.BulletinPaieFilter;
import com.boulangerie.rh.model.BulletinPaie;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class BulletinPaieSpecifications {

    private BulletinPaieSpecifications() {}

    public static Specification<BulletinPaie> withFilters(
            BulletinPaieFilter filter,
            List<Long> employeIds
    ) {
        return Specification
                .<BulletinPaie>where(SearchSpecifications.equal("statut", filter.statut()))
                .and(SearchSpecifications.equal("periodeId", filter.periodeId()))
                .and(employeIdsIn(employeIds));
    }

    public static Specification<BulletinPaie> employeIdsIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return null;
            }
            return root.get("employe").get("id").in(ids);
        };
    }
}