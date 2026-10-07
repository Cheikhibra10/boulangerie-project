package com.boulangerie.comptabilite.specification;

import com.boulangerie.comptabilite.dto.CaisseFilter;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class CaisseSpecifications {

    private CaisseSpecifications() {}

    public static Specification<Caisse> withFilters(CaisseFilter filter) {
        return Specification
                .<Caisse>where(SearchSpecifications.equal("statut", filter.statut()));
    }
}