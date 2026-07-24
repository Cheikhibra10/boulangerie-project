// livreurs/dto/CompteRenduDto.java
package com.boulangerie.livreurs.dto;

import com.boulangerie.livreurs.model.StatutCompteRendu;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteRenduDto extends AbstractAuditingDto {
    private Long id;
    private Long livreurId;
    private LocalDate date;
    private BigDecimal reliquatReport;
    private BigDecimal totalAVerser;
    private BigDecimal versementsDuJour;
    private BigDecimal reliquatFin;
    private StatutCompteRendu statut;
    private List<LigneCompteRenduDto> lignes;
}