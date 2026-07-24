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
public class TopProduitDto {
    private Long produitId;
    private String produitNom;
    private BigDecimal quantiteVendue;
    private BigDecimal caTotal;
}
