// livreurs/dto/LigneCompteRenduDto.java
package com.boulangerie.livreurs.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCompteRenduDto extends AbstractAuditingDto {
    private Long id;
    private Long destinationId;
    private Long produitId;
    private BigDecimal qteLivree;
    private BigDecimal qteAbonnement;
    private BigDecimal retours;
    private BigDecimal qteOfferte;
    private BigDecimal qteNette;
    private BigDecimal prixCommission;
    private BigDecimal montantCommission;
    private BigDecimal depensesLivreur;
    private BigDecimal montantApresDeduction;
}