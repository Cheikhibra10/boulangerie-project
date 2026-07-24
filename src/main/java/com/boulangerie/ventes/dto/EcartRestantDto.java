package com.boulangerie.ventes.dto;

import lombok.AllArgsConstructor;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcartRestantDto {
    private Long produitId;
    private String produitLibelle;
    private BigDecimal quantiteTheorique;
    private BigDecimal quantitePhysique;
    private BigDecimal ecart;
}