package com.boulangerie.ventes.specification;

import com.boulangerie.ventes.model.StatutVente;
import com.boulangerie.ventes.model.VenteBoutique;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class VenteBoutiqueSpecifications {

    public static Specification<VenteBoutique> withFilters(StatutVente statut) {
        return Specs.equal("statut", statut);
    }
}