package com.boulangerie.administration.dto;

import com.boulangerie.administration.model.UniteMesure;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto extends AbstractAuditingDto {

    private Long id;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    private String libelle;

    @NotNull(message = "L'unité est obligatoire")
    private UniteMesure unite;

    @NotNull(message = "Le rendement est obligatoire")
    @Positive(message = "Le rendement doit être positif")
    @DecimalMin(value = "0.00", message = "Le rendement doit être supérieur à 0")
    private BigDecimal equivalenceStock;

    private Boolean actif;

}