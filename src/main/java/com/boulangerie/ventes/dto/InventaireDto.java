// ventes/dto/InventaireRestantDto.java
package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventaireDto {

    @NotNull(message = "La liste des restants est obligatoire")
    private List<RestantDto> restants;
}