// abonnements/dto/LigneAbonnementDto.java
package com.boulangerie.abonnements.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneAbonnementDto extends AbstractAuditingDto {
    private Long id;
    private Long clientId;
    private ClientDto client;
    private BigDecimal prixUnitaire;
    private BigDecimal reliquat;
}