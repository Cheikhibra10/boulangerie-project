package com.boulangerie.abonnements.specification;

import com.boulangerie.abonnements.model.Client;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class ClientSpecifications {

    public static Specification<Client> withFilters(
            String nom,
            String prenom,
            String telephone,
            Boolean actif
    ) {
        return Specification
                .<Client>where(Specs.<Client>equal("nom", nom))
                .and(Specs.<Client>equal("prenom", prenom))
                .and(Specs.<Client>equal("telephone", telephone))
                .and(actif == null ? null : (actif ? Specs.<Client>isTrue("actif") : Specs.<Client>isFalse("actif")));
    }
}