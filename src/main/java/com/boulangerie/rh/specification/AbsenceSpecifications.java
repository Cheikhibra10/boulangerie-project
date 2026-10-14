package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.AbsenceFilter;
import com.boulangerie.rh.model.Absence;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class AbsenceSpecifications {

    private AbsenceSpecifications() {}

    public static Specification<Absence> withFilters(
            AbsenceFilter filter,
            List<Long> employeIds
    ) {
        return Specification
                .<Absence>where(SearchSpecifications.equal("type", filter.type()))
                .and(SearchSpecifications.equal("statut", filter.statut()))
                .and(employeIdsIn(employeIds))
                .and(dateOverlap(filter.dateDebut(), filter.dateFin()));
    }

    public static Specification<Absence> employeIdsIn(List<Long> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) return null;
            return root.get("employe").get("id").in(ids);
        };
    }

    /** Absences that overlap [start, end] */
    public static Specification<Absence> dateOverlap(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) {
                // absence.dateDebut <= end AND absence.dateFin >= start
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("dateDebut"), end),
                        cb.greaterThanOrEqualTo(root.get("dateFin"), start)
                );
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("dateFin"), start);
            }
            return cb.lessThanOrEqualTo(root.get("dateDebut"), end);
        };
    }
}