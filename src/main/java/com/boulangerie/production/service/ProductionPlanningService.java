package com.boulangerie.production.service;


import com.boulangerie.production.dto.CreerProductionRequestDto;
import com.boulangerie.production.dto.LotProductionDto;

public interface ProductionPlanningService {
    LotProductionDto creerProduction(
            CreerProductionRequestDto dto
    );
}
