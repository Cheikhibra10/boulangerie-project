package com.boulangerie.administration.specification;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class ProduitSpecifications {

    public static Specification<Produit> withFilters(
            String libelle,
            Boolean actif,
            TypeProduit typeProduit,
            String categorieNom
    ) {
        return Specification
                .<Produit>where(Specs.<Produit>equal("libelle", libelle))
                .and(actif == null ? null : (actif ? Specs.<Produit>isTrue("actif") : Specs.<Produit>isFalse("actif")))
                .and(Specs.<Produit>equal("typeProduit", typeProduit))
                .and(categorieNomContains(categorieNom));
    }

    private static Specification<Produit> categorieNomContains(String libelle) {
        return (root, query, cb) -> {
            if (libelle == null || libelle.isBlank()) return null;
            return cb.like(cb.lower(root.get("categorie").get("libelle")),
                    "%" + libelle.toLowerCase() + "%");
        };
    }
}