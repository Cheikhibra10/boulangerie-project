package com.boulangerie.production.service;

import com.boulangerie.production.dto.FinaliserProductionRequestDto;
import com.boulangerie.production.dto.LotProductionDto;
import org.springframework.stereotype.Service;

public interface ProductionExecutionService {
    LotProductionDto finaliserProduction(
            Long lotId,
            FinaliserProductionRequestDto dto
    );
}
