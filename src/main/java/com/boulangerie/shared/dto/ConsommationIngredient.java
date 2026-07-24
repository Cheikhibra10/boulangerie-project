package com.boulangerie.shared.dto;

import java.math.BigDecimal;
import java.util.Objects;

public record ConsommationIngredient(

        Long ingredientId,

        BigDecimal quantite

) {

    public ConsommationIngredient {

        Objects.requireNonNull(ingredientId);
        Objects.requireNonNull(quantite);

        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La quantité consommée doit être positive."
            );
        }
    }

}