package com.boulangerie.stocks.specification;

import com.boulangerie.shared.specification.SearchSpecifications;
import com.boulangerie.stocks.dto.MouvementStockFilter;
import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class MouvementStockSpecifications {

    private MouvementStockSpecifications() {}

    public static Specification<MouvementStock> withFilters(MouvementStockFilter filter) {
        return Specification
                .<MouvementStock>where(SearchSpecifications.equal("type", filter.type()))
                .and(SearchSpecifications.equal("statut", filter.statut()))
                .and(SearchSpecifications.like("ingredient.libelle", filter.ingredientLibelle()))
                .and(SearchSpecifications.like("motif", filter.motif()))
                .and(dateBetween(filter.dateDebut(), filter.dateFin()));
    }

    public static Specification<MouvementStock> byType(TypeMouvementStock type) {
        return SearchSpecifications.equal("type", type);
    }

    public static Specification<MouvementStock> byStatut(StatutMouvement statut) {
        return SearchSpecifications.equal("statut", statut);
    }

    public static Specification<MouvementStock> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }

            Instant startInstant = start != null
                    ? start.atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endInstant = end != null
                    ? end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            if (startInstant != null && endInstant != null) {
                return cb.between(root.get("date"), startInstant, endInstant);
            }
            if (startInstant != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), startInstant);
            }
            return cb.lessThan(root.get("date"), endInstant);
        };
    }
}