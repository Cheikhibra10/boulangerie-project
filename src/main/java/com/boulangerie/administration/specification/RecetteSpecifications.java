package com.boulangerie.administration.specification;

import com.boulangerie.administration.model.Recette;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class RecetteSpecifications {

    public static Specification<Recette> withFilters(Integer version, Boolean actif, String produitNom) {
        return Specification
                .<Recette>where(Specs.<Recette>equal("version", version))
                .and(Specs.<Recette>equal("actif", actif))
                .and(produitNomContains(produitNom));
    }

    private static Specification<Recette> produitNomContains(String libelle) {
        return (root, query, cb) -> {
            if (libelle == null || libelle.isBlank()) return null;
            return cb.like(cb.lower(root.get("produit").get("libelle")),
                    "%" + libelle.toLowerCase() + "%");
        };
    }
}