package com.boulangerie.production.specification;

import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.model.StatutProduction;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class LotProductionSpecifications {

    public static Specification<LotProduction> withFilters(
            String produitNom,
            StatutProduction statut
    ) {
        return Specification
                .where(produitNomContains(produitNom))
                .and(Specs.equal("statut", statut));
    }

    private static Specification<LotProduction> produitNomContains(String libelle) {
        return (root, query, cb) -> {
            if (libelle == null || libelle.isBlank()) return null;
            return cb.like(cb.lower(root.get("produit").get("libelle")),
                    "%" + libelle.toLowerCase() + "%");
        };
    }
}