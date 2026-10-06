package com.boulangerie.abonnements.specification;

import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class AbonnementSpecifications {

    public static Specification<Abonnement> withFilters(Boolean actif, String livreurNom) {
        return Specification
                .where(actif == null ? null : (actif ? Specs.<Abonnement>isTrue("actif") : Specs.<Abonnement>isFalse("actif")))
                .and(livreurNomContains(livreurNom));
    }

    private static Specification<Abonnement> livreurNomContains(String nom) {
        return (root, query, cb) -> {
            if (nom == null || nom.isBlank()) return null;
            return cb.or(
                    cb.like(cb.lower(root.get("livreur").get("nom")), "%" + nom.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("livreur").get("prenom")), "%" + nom.toLowerCase() + "%")
            );
        };
    }
}