// ventes/dto/RestantDto.java
package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestantDto {

    @NotNull(message = "Le produit est obligatoire")
    private Long produitId;

    @NotNull(message = "La quantité physique est obligatoire")
    @PositiveOrZero(message = "La quantité doit être positive ou nulle")
    private BigDecimal quantitePhysique;
}