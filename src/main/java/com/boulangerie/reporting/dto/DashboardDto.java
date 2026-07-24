package com.boulangerie.reporting.dto;

import com.boulangerie.reporting.dto.KpiDto;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.util.List;

// reporting/api/dto/DashboardDto.java
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto extends AbstractAuditingDto {
    private KpiDto kpis;
    private List<TopProduitDto> topProduits;
    private List<EvolutionVenteDto> evolutionVentes;
    private List<StatutStatDto> statutsAchats;
}
