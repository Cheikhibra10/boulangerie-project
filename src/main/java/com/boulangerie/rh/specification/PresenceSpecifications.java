package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.PresenceFilter;
import com.boulangerie.rh.model.Presence;
import com.boulangerie.rh.model.StatutPresence;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class PresenceSpecifications {

    private PresenceSpecifications() {}

    public static Specification<Presence> withFilters(
            PresenceFilter filter,
            List<Long> employeIds
    ) {
        return Specification
                .<Presence>where(SearchSpecifications.equal("statut", filter.statut()))
                .and(employeIdsIn(employeIds))
                .and(dateBetween(filter.dateDebut(), filter.dateFin()));
    }

    public static Specification<Presence> employeIdsIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return null;
            }
            return root.get("employe").get("id").in(ids);
        };
    }

    public static Specification<Presence> dateBetween(LocalDate start, LocalDate end) {
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