package com.boulangerie.administration.dto;

import com.boulangerie.administration.model.UniteMesure;

public record IngredientFilter(
        String libelle,
        UniteMesure unite,
        Boolean actif

) {
}
