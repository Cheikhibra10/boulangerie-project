package com.boulangerie.production.service.impl;

import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.administration.service.RecetteService;
import com.boulangerie.production.dto.CreerProductionRequestDto;
import com.boulangerie.production.dto.LotProductionDto;
import com.boulangerie.production.dto.ProductionPlan;
import com.boulangerie.production.exception.LotAlreadyExistsException;
import com.boulangerie.production.mapper.LotProductionMapper;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.model.StatutProduction;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.production.service.ProductionFactory;
import com.boulangerie.production.service.ProductionPlanningService;
import com.boulangerie.stocks.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductionPlanningServiceImpl implements ProductionPlanningService {

    private final LotProductionRepository lotRepository;
    private final RecetteService recetteService;
    private final StockService stockService;
    private final ProductionFactory productionFactory;
    private final LotProductionMapper mapper;
    private final CurrentUserService currentUserService;

    @Override
    public LotProductionDto creerProduction(CreerProductionRequestDto dto) {

        verifierDoublon(dto);
        Recette recette = recetteService.findByIdOrThrow(dto.getRecetteId());

        recette.verifierPeutProduire();
        Long produitId = recette.getProduit().getId();

        ProductionPlan plan = productionFactory.creerPlan(recette, dto.getSacsFarineUtilises());

        stockService.verifierDisponibiliteIngredients(plan.consommations());

        LotProduction lot = new LotProduction()
                        .setProduitId(produitId)
                        .setRecetteId(recette.getId())
                        .setDate(dto.getDate())
                        .setSacsFarineUtilises(dto.getSacsFarineUtilises())
                        .setQuantitePrevue(recette.getRendement())
                        .setStatut(StatutProduction.PLANIFIEE);

        lot = lotRepository.save(lot);

        log.info(
                "Production {} créée par {}",
                lot.getId(),
                currentUserService
                        .getCurrentUser()
                        .getNom()
        );

        return mapper.toDto(lot);
    }

    private void verifierDoublon(CreerProductionRequestDto dto) {
        Recette recette = recetteService.findByIdOrThrow(dto.getRecetteId());
        Long produitId = recette.getProduit().getId();
        if (lotRepository.existsByDateAndProduitId(dto.getDate(), produitId
        )) {
            throw new LotAlreadyExistsException(dto.getDate(), produitId);
        }
    }
}