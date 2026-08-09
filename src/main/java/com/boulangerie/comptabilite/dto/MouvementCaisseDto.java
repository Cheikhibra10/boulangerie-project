// caisse/dto/MouvementCaisseDto.java
package com.boulangerie.comptabilite.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.model.TypePaiement;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementCaisseDto extends AbstractAuditingDto {
    private Long id;
    private TypeMouvement typeMouvement;
    private SensMouvement sens;
    private BigDecimal montant;
    private TypePaiement modePaiement;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;
    private Long caisseId;
    private String libelle;
    private Long livreurId;
}