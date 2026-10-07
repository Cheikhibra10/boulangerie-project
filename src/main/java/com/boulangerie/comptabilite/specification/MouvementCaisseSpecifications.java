package com.boulangerie.comptabilite.specification;

import com.boulangerie.comptabilite.dto.MouvementCaisseFilter;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public final class MouvementCaisseSpecifications {

    private MouvementCaisseSpecifications() {}

    public static Specification<MouvementCaisse> withFilters(
            MouvementCaisseFilter filter
    ) {
        return Specification
                .<MouvementCaisse>where(byType(filter.typeMouvement()))
                .and(bySens(filter.sens()))
                .and(SearchSpecifications.like("libelle", filter.libelle()))
                .and(dateBetween(filter.dateDebut(), filter.dateFin()))
                .and(byCaisseStatut(filter.caisseStatut()));
    }

    public static Specification<MouvementCaisse> byType(TypeMouvement type) {
        return SearchSpecifications.equal("typeMouvement", type);
    }

    public static Specification<MouvementCaisse> bySens(SensMouvement sens) {
        return SearchSpecifications.equal("sens", sens);
    }

    public static Specification<MouvementCaisse> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;

            Instant startInstant = start != null
                    ? start.atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endInstant = end != null
                    ? end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            if (startInstant != null && endInstant != null) {
                return cb.between(root.get("createdAt"), startInstant, endInstant);
            }
            if (startInstant != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startInstant);
            }
            return cb.lessThan(root.get("createdAt"), endInstant);
        };
    }

    public static Specification<MouvementCaisse> byCaisseStatut(StatutCaisse statut) {
        return (root, query, cb) -> {
            if (statut == null) return null;
            return cb.equal(root.get("caisse").get("statut"), statut);
        };
    }



}