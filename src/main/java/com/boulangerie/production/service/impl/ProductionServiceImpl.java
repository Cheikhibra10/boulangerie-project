package com.boulangerie.production.service.impl;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.production.dto.*;
import com.boulangerie.production.mapper.DestinationMapper;
import com.boulangerie.production.mapper.LotProductionMapper;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.repository.DestinationProductionRepository;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.production.service.ProductionService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductionServiceImpl implements ProductionService {

    private final LotProductionRepository lotRepository;
    private final DestinationProductionRepository destinationRepository;
    private final LotProductionMapper lotMapper;
    private final DestinationMapper destinationMapper;
    private final ProduitService produitService;

    @Override
    public List<DestinationDto> distribuerProduction(Long productionId, DistribuerProductionRequestDto request) {
        LotProduction production = getProduction(productionId);
        production.verifierDistributionPossible();
        // Récupérer la quantité déjà répartie
        BigDecimal dejaDistribue = destinationRepository.sumQuantiteByLotId(productionId);
        production.verifierQuantiteDistribuable(dejaDistribue, calculerDemande(request));
        List<DestinationProduction> nouvellesDestinations = production.ajouterDestinations(request.getDestinations());
        lotRepository.save(production);
        return nouvellesDestinations.stream()
                .map(destinationMapper::toDto)
                .toList();
    }

    private BigDecimal calculerDemande(DistribuerProductionRequestDto request) {
        return request.getDestinations()
                .stream()
                .map(DestinationRequestDto::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public LotProductionDto getLot(Long id) {
        return lotRepository.findById(id)
                .map(lotMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("LotProduction" +id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LotProductionDto> getLots(int page, int size) {
        Page<LotProduction> pageResult = lotRepository.findAll(PageRequest.of(page, size));
        return PageUtils.toPageResponse(pageResult.map(lotMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationDto> getDestinationsByLot(Long lotId) {
        return destinationRepository.findByLotId(lotId).stream()
                .map(destinationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationDto> getDestinationsByLivreurEtDate(Long livreurId, LocalDate date) {
        return destinationRepository.findByDateAndLivreurId(date, livreurId).stream()
                .map(destinationMapper::toDto)
                .toList();
    }

    private LotProduction getProduction(Long lotId) {
        return lotRepository.findById(lotId)
                .orElseThrow(() -> new EntityNotFoundException("Production introuvable" +lotId));
    }

}