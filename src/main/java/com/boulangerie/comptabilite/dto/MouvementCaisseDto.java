// caisse/dto/MouvementCaisseDto.java
package com.boulangerie.comptabilite.dto;

import com.boulangerie.comptabilite.model.SensMouvement;
import com.boulangerie.comptabilite.model.TypeMouvement;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.TypePaiement;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementCaisseDto extends AbstractAuditingDto {
    private Long id;
    private TypeMouvement typeMouvement;
    private SensMouvement sens;
    private BigDecimal montant;
    private TypePaiement modePaiement;
    private Instant date;
    private Long caisseId;
    private String libelle;
    private Long livreurId;
}