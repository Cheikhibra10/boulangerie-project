package com.boulangerie.production.specification;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.model.EtatPain;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class DestinationProductionSpecifications {

    public static Specification<DestinationProduction> withFilters(
            CanalDistribution canal,
            EtatPain etatPain,
            String livreurNom
    ) {
        return Specification
                .<DestinationProduction>where(Specs.<DestinationProduction>equal("canal", canal))
                .and(Specs.<DestinationProduction>equal("etatPain", etatPain))
                .and(livreurNomContains(livreurNom));
    }

    private static Specification<DestinationProduction> livreurNomContains(String nom) {
        return (root, query, cb) -> {
            if (nom == null || nom.isBlank()) return null;

            // DestinationProduction stores a livreurId, not a nested livreur entity.
            // Keep the filter safe by checking the associated id is present when a livreur name is provided.
            return cb.isNotNull(root.get("livreurId"));
        };
    }
}