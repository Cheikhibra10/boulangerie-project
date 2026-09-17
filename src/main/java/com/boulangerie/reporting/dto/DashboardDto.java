package com.boulangerie.reporting.dto;

import com.boulangerie.reporting.dto.KpiDto;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DashboardDto {
    private KpiDto kpis;
    private List<TopProduitDto> topProduits;
    private List<EvolutionVenteDto> evolutionVentes;
    private AchatStatistiquesDto statutsAchats;
}
