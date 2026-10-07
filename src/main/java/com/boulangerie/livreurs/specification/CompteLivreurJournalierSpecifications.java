package com.boulangerie.livreurs.specification;

import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.StatutCompteRendu;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public final class CompteLivreurJournalierSpecifications {

    private CompteLivreurJournalierSpecifications() {}

    public static Specification<CompteLivreurJournalier> withFilters(
            StatutCompteRendu statut,
            List<Long> livreurIds,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {
        return Specification
                .<CompteLivreurJournalier>where(SearchSpecifications.equal("statut", statut))
                .and(livreurIdsIn(livreurIds))
                .and(dateBetween(dateDebut, dateFin));
    }

    public static Specification<CompteLivreurJournalier> livreurIdsIn(List<Long> livreurIds) {
        return (root, query, cb) -> {
            if (livreurIds == null || livreurIds.isEmpty()) {
                return null;
            }
            return root.get("livreurId").in(livreurIds);
        };
    }

    public static Specification<CompteLivreurJournalier> dateBetween(LocalDate start, LocalDate end) {
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