package com.boulangerie.administration.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetteIngredientDto {

    private Long id;

    @NotNull(message = "L'ingrédient est obligatoire")
    private Long ingredientId;

    private String ingredientLibelle;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    @DecimalMin(value = "0.00", message = "La quantité doit être supérieure à 0")
    private BigDecimal quantite;

}