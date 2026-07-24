// production/dto/RealiseLotDto.java
package com.boulangerie.production.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinaliserProductionRequestDto {

    @NotNull(message = "La quantité réalisée est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteRealisee;
}