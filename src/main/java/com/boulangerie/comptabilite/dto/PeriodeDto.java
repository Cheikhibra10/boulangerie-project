package com.boulangerie.comptabilite.dto;

import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodeDto extends AbstractAuditingDto {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutPeriode statut;
    private BigDecimal beneficeReport;
}