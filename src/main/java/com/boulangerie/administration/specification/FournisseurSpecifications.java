package com.boulangerie.administration.specification;

import com.boulangerie.administration.dto.FournisseurFilter;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class FournisseurSpecifications {

    private FournisseurSpecifications() {}

    public static Specification<Fournisseur> withFilters(FournisseurFilter filter) {
        return Specification
                .<Fournisseur>where(SearchSpecifications.like("nom", filter.nom()))
                .and(SearchSpecifications.like("telephone", filter.telephone()))
                .and(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"));
    }
}