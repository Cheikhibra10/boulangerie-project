// ventes/dto/LigneVenteDto.java
package com.boulangerie.ventes.dto;

import com.boulangerie.ventes.model.TypeVenteLigne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneVenteDto {
    private Long id;
    private Long produitId;
    private String produitLibelle;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal total;
    private TypeVenteLigne typeVente;
}