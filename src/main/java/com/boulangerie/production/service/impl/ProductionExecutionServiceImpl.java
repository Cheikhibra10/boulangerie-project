package com.boulangerie.production.service.impl;

import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.administration.service.RecetteService;
import com.boulangerie.production.dto.*;
import com.boulangerie.production.mapper.LotProductionMapper;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.production.service.ProductionExecutionService;
import com.boulangerie.production.service.ProductionFactory;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.stocks.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductionExecutionServiceImpl
        implements ProductionExecutionService {

    private final LotProductionRepository lotRepository;
    private final RecetteService recetteService;
    private final ProductionFactory productionFactory;
    private final StockService stockService;
    private final LotProductionMapper mapper;
    private final CurrentUserService currentUserService;

    @Override
    public LotProductionDto finaliserProduction(
            Long lotId,
            FinaliserProductionRequestDto dto
    ) {

        LotProduction lot = chargerLot(lotId);

        Recette recette = recetteService.findByIdOrThrow(lot.getRecetteId());

        ProductionPlan plan = productionFactory.creerPlan(recette, lot.getSacsFarineUtilises());

        stockService.consommerIngredients(lot.getId(), plan.consommations());

        stockService.augmenterStockProduit(lot.getProduitId(), dto.getQuantiteRealisee());

        lot.finaliser(dto.getQuantiteRealisee());
        lotRepository.save(lot);
        log.info(
                "Production {} finalisée par {}",
                lotId,
                currentUserService
                        .getCurrentUser()
                        .getNom()
        );

        return mapper.toDto(lot);
    }

    private LotProduction chargerLot(Long lotId) {
        return lotRepository.findById(lotId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Production introuvable : " + lotId));
    }
}