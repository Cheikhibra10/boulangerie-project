// reporting/api/dto/KpiDto.java
package com.boulangerie.reporting.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiDto extends AbstractAuditingDto {

    // ===== VENTES =====
    private BigDecimal caTotal;
    private BigDecimal caBoutique;
    private BigDecimal caLivreurs;
    private BigDecimal caAbonnements;
    private BigDecimal caRestants;

    // ===== PRODUCTION =====
    private BigDecimal quantiteProduite;
    private BigDecimal quantiteVendue;
    private Double tauxEcoulement;

    // ===== STOCKS =====
    private List<AlerteStockDto> alertesStocks;
    private Integer nbIngredientsSousSeuil;

    // ===== LIVREURS =====
    private BigDecimal reliquatTotalLivreurs;
    private Integer nbLivreursActifs;

    // ===== ABONNEMENTS =====
    private BigDecimal creditsClientsTotal;
    private Integer nbAbonnementsActifs;

    // ===== CAISSE =====
    private BigDecimal totalEntrees;
    private BigDecimal totalSorties;
    private BigDecimal soldeTheorique;
}