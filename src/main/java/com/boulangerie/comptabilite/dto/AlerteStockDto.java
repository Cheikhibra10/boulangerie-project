package com.boulangerie.comptabilite.dto;

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
    private String unite;
}
