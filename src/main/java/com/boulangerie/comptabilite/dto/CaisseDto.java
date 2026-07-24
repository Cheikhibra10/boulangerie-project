// caisse/dto/CaisseDto.java
package com.boulangerie.comptabilite.dto;

import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaisseDto extends AbstractAuditingDto {
    private Long id;
    private Instant dateOuverture;
    private Instant dateFermeture;
    private BigDecimal soldeInitial;
    private BigDecimal soldeFinal;
    private BigDecimal soldePhysique;
    private BigDecimal ecart;
    private StatutCaisse statut;
    private Long ouverteParId;
    private String ouverteParNom;
    private Long fermeeParId;
    private String fermeeParNom;
}