// abonnements/dto/PaiementClientResultDto.java
package com.boulangerie.abonnements.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PaiementClientResultDto extends AbstractAuditingDto {
    private Long ligneId;
    private BigDecimal montantPaye;
    private BigDecimal nouveauReliquat;
    private boolean estSolde;
}