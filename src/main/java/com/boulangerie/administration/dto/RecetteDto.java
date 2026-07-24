// administration/dto/RecetteDto.java
package com.boulangerie.administration.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetteDto extends AbstractAuditingDto {

    private Long id;

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "La version est obligatoire")
    @Positive(message = "La version doit être positive")
    private Integer version;

    private Boolean actif;

    @NotNull(message = "Le rendement est obligatoire")
    @Positive(message = "Le rendement doit être positif")
    @DecimalMin(value = "0.00", message = "Le rendement doit être supérieur à 0")
    private BigDecimal rendement;

    private Integer tempsPreparation;
    private Integer tempsCuisson;

    @NotEmpty(message = "Une recette doit contenir au moins un ingrédient.")
    private List<RecetteIngredientDto> ingredients = new ArrayList<>();

}