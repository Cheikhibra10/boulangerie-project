// production/dto/DestinationDto.java
package com.boulangerie.production.dto;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDto extends AbstractAuditingDto {
    private Long id;
    private CanalDistribution canal;
    private Long produitId;
    private BigDecimal quantite;
    private BigDecimal prixUnitaire;
    private Long livreurId;
    private Long abonnementId;
}