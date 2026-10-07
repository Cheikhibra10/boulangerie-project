package com.boulangerie.administration.specification;

import com.boulangerie.administration.dto.IngredientFilter;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class IngredientSpecifications {

    private IngredientSpecifications() {}

    public static Specification<Ingredient> withFilters(IngredientFilter filter) {
        return Specification
                .<Ingredient>where(SearchSpecifications.like("libelle", filter.libelle()))
                .and(SearchSpecifications.equal("unite", filter.unite()))
                .and(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"));
    }
}