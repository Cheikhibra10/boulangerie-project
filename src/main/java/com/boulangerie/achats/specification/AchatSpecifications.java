package com.boulangerie.achats.specification;

import com.boulangerie.achats.model.*;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class AchatSpecifications {

    public static Specification<Achat> withFilters(
            String fournisseurNom,
            StatutReception statutReception,
            StatutPaiement statutPaiement,
            StatutAchat statutAchat
    ) {
        return Specification
                .where(fournisseurNomContains(fournisseurNom))
                .and(Specs.equal("statutReception", statutReception))
                .and(Specs.equal("statutPaiement", statutPaiement))
                .and(Specs.equal("statutAchat", statutAchat));
    }

    private static Specification<Achat> fournisseurNomContains(String nom) {
        return (root, query, cb) -> {
            if (nom == null || nom.isBlank()) return null;
            return cb.like(cb.lower(root.get("fournisseur").get("nom")),
                    "%" + nom.toLowerCase() + "%");
        };
    }
}