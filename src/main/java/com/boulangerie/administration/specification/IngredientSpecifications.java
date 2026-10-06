package com.boulangerie.administration.specification;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.administration.model.UniteMesure;
import com.boulangerie.shared.specification.Specs;
import org.springframework.data.jpa.domain.Specification;

public class IngredientSpecifications {
    public static Specification<Ingredient> withFilters(
            String libelle,
            UniteMesure unite,
            Boolean actif
    ) {
        return Specification
                .<Ingredient>where(Specs.<Ingredient>equal("libelle", libelle))
                .and(actif == null ? null : (actif ? Specs.<Ingredient>isTrue("actif") : Specs.<Ingredient>isFalse("actif")))
                .and(Specs.<Ingredient>equal("unite", unite));
    }
}
