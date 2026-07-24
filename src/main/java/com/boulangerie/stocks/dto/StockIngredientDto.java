// stocks/dto/StockIngredientDto.java
package com.boulangerie.stocks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockIngredientDto {
    private Long id;
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantite;
    private BigDecimal valeurTotale;
    private BigDecimal seuilAlerte;
    private Boolean alerte;
}