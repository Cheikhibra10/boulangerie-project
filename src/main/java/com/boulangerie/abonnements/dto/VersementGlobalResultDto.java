// abonnements/dto/VersementGlobalResultDto.java
package com.boulangerie.abonnements.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class VersementGlobalResultDto extends AbstractAuditingDto {
    private Long abonnementId;
    private BigDecimal montantVerse;
    private BigDecimal montantRestant;
    private List<LigneAbonnementDto> clientsRepartis;
}