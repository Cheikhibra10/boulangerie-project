// production/dto/CreationLotDto.java
package com.boulangerie.production.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreerProductionRequestDto {

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "La recette est obligatoire")
    private Long recetteId;

    @NotNull(message = "Le nombre de sacs de farine est obligatoire")
    @Positive(message = "Le nombre de sacs doit être positif")
    private BigDecimal sacsFarineUtilises;

}