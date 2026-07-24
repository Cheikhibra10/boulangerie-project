package com.boulangerie.achats.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.TypePaiement;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementFournisseurDto extends AbstractAuditingDto {
    Long id;
    BigDecimal montant;
    BigDecimal montantPaye;
    BigDecimal restantDu;
    TypePaiement modePaiement;
}