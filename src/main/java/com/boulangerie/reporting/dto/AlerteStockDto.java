// reporting/api/dto/AlerteStockDto.java
package com.boulangerie.reporting.dto;

import com.boulangerie.administration.model.UniteMesure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteStockDto {
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantiteActuelle;
    private BigDecimal seuilAlerte;
    private UniteMesure unite;
}