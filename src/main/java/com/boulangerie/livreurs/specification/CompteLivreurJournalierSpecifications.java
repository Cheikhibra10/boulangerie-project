package com.boulangerie.livreurs.specification;

import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.StatutCompteRendu;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class CompteLivreurJournalierSpecifications {

    public static Specification<CompteLivreurJournalier> withFilters(
            StatutCompteRendu statut,
            String livreurNom
    ) {
        return Specification
                .<CompteLivreurJournalier>where(Specs.<CompteLivreurJournalier>equal("statut", statut))
                .and(livreurNomContains(livreurNom));
    }

    private static Specification<CompteLivreurJournalier> livreurNomContains(String nom) {
        return (root, query, cb) -> {
            if (nom == null || nom.isBlank()) return null;
            return cb.or(
                    cb.like(cb.lower(root.get("livreur").get("nom")), "%" + nom.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("livreur").get("prenom")), "%" + nom.toLowerCase() + "%")
            );
        };
    }
}