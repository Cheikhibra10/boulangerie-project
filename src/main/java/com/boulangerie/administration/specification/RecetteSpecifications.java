package com.boulangerie.administration.specification;

import com.boulangerie.administration.dto.RecetteFilter;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class RecetteSpecifications {

    private RecetteSpecifications() {}

    public static Specification<Recette> withFilters(RecetteFilter filter) {
        return Specification
                .<Recette>where(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"))
                .and(SearchSpecifications.like("produit.libelle", filter.produitLibelle()));
    }
}