package com.boulangerie.achats.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneAchatDto extends AbstractAuditingDto {
    private Long id;
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantiteCommandee;
    private BigDecimal quantiteRecue;
    private BigDecimal quantiteRetournee;
    private BigDecimal quantiteAcceptee;
    private String motifRetour;
    private BigDecimal ecart;
    private BigDecimal montantCommande;
    private BigDecimal montantRecu;
    private BigDecimal prixUnitaire;
}