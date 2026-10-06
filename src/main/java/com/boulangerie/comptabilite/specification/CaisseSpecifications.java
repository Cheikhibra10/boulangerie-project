package com.boulangerie.comptabilite.specification;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class CaisseSpecifications {

    public static Specification<Caisse> withFilters(StatutCaisse statut) {
        return Specs.equal("statut", statut);
    }
}