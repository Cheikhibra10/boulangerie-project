package com.boulangerie.livreurs.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CommissionRegleDto extends AbstractAuditingDto {

    private Long id;

    private Long livreurId;

    private Long produitId;

    private BigDecimal montantParUnite;

    private LocalDate dateDebut;

    private LocalDate dateFin;
}