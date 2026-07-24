package com.boulangerie.ventes.service.impl;

import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.stocks.service.StockService;
import com.boulangerie.ventes.dto.*;
import com.boulangerie.ventes.event.VentePayeeEvent;
import com.boulangerie.ventes.mapper.StockMovementMapper;
import com.boulangerie.ventes.mapper.VenteMapper;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.Paiement;
import com.boulangerie.ventes.model.VenteBoutique;
import com.boulangerie.ventes.repository.VenteBoutiqueRepository;
import com.boulangerie.ventes.service.PaiementFactory;
import com.boulangerie.ventes.service.VenteFactory;
import com.boulangerie.ventes.service.VenteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VenteServiceImpl implements VenteService {

    private final VenteBoutiqueRepository venteRepository;
    private final VenteMapper venteMapper;
    private final StockMovementMapper stockMovementMapper;
    private final StockService stockService;
    private final CurrentUserService currentUserService;
    private final VenteFactory venteFactory;
    private final PaiementFactory paiementFactory;
    private final ApplicationEventPublisher publisher;
    private final ProductionAllocationApi productionApi;

    @Transactional
    @Override
    public VenteDto creerVente(VenteRequestDto dto) {
        VenteBoutique vente = venteFactory.creer(dto.getCaisseId(), currentUserService.getCurrentUser(), dto.getLignes());
        verifierDisponibilite(vente.getLignes());
        Paiement paiement = paiementFactory.creer(vente, dto.getPaiement());
        vente.encaisser(paiement);
        venteRepository.save(vente);
        enregistrerConsommationBoutique(vente.getLignes());
        stockService.decrementerStock(stockMovementMapper.toStockMovements(vente.getLignes()));
        publisher.publishEvent(
                new VentePayeeEvent(
                        vente.getId(),
                        vente.getCaisseId(),
                        paiement.getMontant(),
                        paiement.getModePaiement(),
                        paiement.getLibelle()
                )
        );
        return venteMapper.toDto(vente);
    }


    private void verifierDisponibilite(List<LigneVenteBoutique> lignes) {

        for (LigneVenteBoutique ligne : lignes) {
            if (!productionApi.verifierDisponibiliteBoutique(ligne.getProduitId(), ligne.getQuantite())) {
                throw new BadRequestException("Stock boutique insuffisant pour le produit "
                                + ligne.getProduitId());
            }
        }
    }

    private void enregistrerConsommationBoutique(
            List<LigneVenteBoutique> lignes) {
        for (LigneVenteBoutique ligne : lignes) {
            productionApi.enregistrerVenteBoutique(ligne.getProduitId(), ligne.getQuantite());
        }
    }

    @Override
    public InventaireResultDto enregistrerRestants(Long venteId, InventaireDto dto) {
        VenteBoutique vente = getVenteBoutique(venteId);
        List<EcartRestantDto> ecarts = new ArrayList<>();

        for (RestantDto restant : dto.getRestants()) {
            // Récupérer le stock théorique du produit
            // Pour simplifier, on utilise le stock actuel
            // Dans la réalité, on devrait avoir une notion de stock théorique avant inventaire

            // Ici on simule un écart (à implémenter avec le stock service)
            ecarts.add(EcartRestantDto.builder()
                    .produitId(restant.getProduitId())
                    .quantiteTheorique(BigDecimal.ZERO) // À calculer
                    .quantitePhysique(restant.getQuantitePhysique())
                    .ecart(BigDecimal.ZERO) // À calculer
                    .build());
        }

        log.info("Inventaire des restants enregistré pour la vente {} par {}",
                venteId, currentUserService.getCurrentUser().getNom());

        return InventaireResultDto.builder().ecarts(ecarts).build();
    }

    // ===================== CONSULTATION =====================

    @Override
    @Transactional(readOnly = true)
    public VenteDto getVente(Long id) {
        return venteRepository.findByIdWithDetails(id)
                .map(venteMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable" +id));
    }

    private VenteBoutique getVenteBoutique(Long venteId) {
        return venteRepository.findById(venteId)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable" +venteId));
    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<VenteDto> getVentes(LocalDate date, Long produitId, int page, int size) {
        Page<VenteBoutique> pageResult;

        if (produitId != null) {
            pageResult = venteRepository.findAll(PageRequest.of(page, size));
        } else if (date != null) {
            pageResult = venteRepository.findByDate(date, PageRequest.of(page, size));
        } else {
            pageResult = venteRepository.findAll(PageRequest.of(page, size));
        }
        return PageUtils.toPageResponse(pageResult.map(venteMapper::toDto));
    }
}