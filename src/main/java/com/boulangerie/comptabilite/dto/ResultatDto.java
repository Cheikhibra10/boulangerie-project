package com.boulangerie.comptabilite.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultatDto extends AbstractAuditingDto {
    private Long id;
    private Long periodeId;

    private BigDecimal caAbonnements;
    private BigDecimal caVentesLivreurs;
    private BigDecimal caVentesBoutique;
    private BigDecimal caVenteRestants;
    private BigDecimal caAutresProduits;
    private BigDecimal caTotal;

    private BigDecimal totalCharges;
    private BigDecimal beneficeBrut;

    private BigDecimal reliquatLivreursDeduit;
    private BigDecimal creditsDeduits;
    private BigDecimal beneficeDistribuable;

    private BigDecimal partGerant;
    private BigDecimal partBoulangerie;
    private BigDecimal beneficeNet;
}