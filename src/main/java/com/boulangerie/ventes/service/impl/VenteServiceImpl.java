package com.boulangerie.ventes.service.impl;

import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.stocks.dto.StockMovement;
import com.boulangerie.stocks.service.StockService;
import com.boulangerie.ventes.dto.*;
import com.boulangerie.shared.dto.MouvementCaisseEvent;
import com.boulangerie.ventes.mapper.StockMovementMapper;
import com.boulangerie.ventes.mapper.VenteMapper;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.Paiement;
import com.boulangerie.ventes.model.VenteBoutique;
import com.boulangerie.ventes.repository.VenteBoutiqueRepository;
import com.boulangerie.ventes.service.*;
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
    private final RestaurationVenteService restaurationService;
    private final PaiementVenteService paiementService;
    private final CalculerPaiementService calculateurPaiement;

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
                new MouvementCaisseEvent(
                        vente.getCaisseId(),
                        TypeMouvement.PAIEMENT,
                        SensMouvement.ENTREE,
                        TypePaiement.CASH,
                        vente.getPaiement().getMontant(),
                        "Paiement vente #" + vente.getNumero()
                )
        );
        return venteMapper.toDto(vente);
    }

    @Transactional
    @Override
    public VenteDto retournerVente(Long venteId, RetourVenteRequestDto dto) {

        VenteBoutique vente = chargerVente(venteId);

        vente.verifierRetourPossible();

        appliquerRetours(vente, dto.getRetours());

        restaurationService.restaurerStock(dto.getRetours());

        restaurationService.restaurerBoutique(dto.getRetours());

        if (!dto.getEchanges().isEmpty()) {
            verifierDisponibiliteEchange(dto.getEchanges());
            consommerEchanges(dto.getEchanges());
            enregistrerConsommationBoutiqueEchange(dto.getEchanges());
        }

        BigDecimal difference = calculateurPaiement.calculerDifference(vente, dto);

        if (difference.signum() > 0) {
            paiementService.complementPaiement(vente, difference);
        } else if (difference.signum() < 0) {
            paiementService.rembourserRetour(vente, difference.abs());
        }

        return venteMapper.toDto(vente);
    }



    @Transactional
    @Override
    public VenteDto annulerVente(Long venteId, AnnulationVenteRequestDto dto) {

        VenteBoutique vente = chargerVente(venteId);
        vente.annuler(dto.getMotif());
        restaurationService.restaurerStockAnnulation(vente.getLignes());

        restaurationService.restaurerBoutiqueAnnulation(vente.getLignes());

        paiementService.rembourserAnnulation(vente, dto.getMotif());
        return venteMapper.toDto(vente);
    }

    private void appliquerRetours(
            VenteBoutique vente,
            List<LigneRetourRequestDto> retours) {

        for (LigneRetourRequestDto retour : retours) {
            LigneVenteBoutique ligne = vente.getLigne(retour.getProduitId());
            ligne.retourner(retour.getQuantite());
        }
    }

    private void verifierDisponibiliteEchange(List<LigneVenteRequestDto> echanges) {

        echanges.forEach(ligne ->
                stockService.verifierStockSuffisant(
                        ligne.getProduitId(),
                        ligne.getQuantite()));
    }

    private void consommerEchanges(List<LigneVenteRequestDto> echanges) {
        List<StockMovement> mouvements = echanges.stream()
                .map(ligne -> new StockMovement(
                        ligne.getProduitId(),
                        ligne.getQuantite()))
                .toList();
        stockService.decrementerStock(mouvements);
    }

    private VenteBoutique chargerVente(Long venteId) {
        return venteRepository.findById(venteId)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable" +venteId));
    }


    private void enregistrerConsommationBoutique(List<LigneVenteBoutique> lignes) {
        for (LigneVenteBoutique ligne : lignes) {
            productionApi.vendre(
                    ligne.getProduitId(),
                    ligne.getQuantite());
        }
    }

    private void enregistrerConsommationBoutiqueEchange(
            List<LigneVenteRequestDto> lignes) {

        for (LigneVenteRequestDto ligne : lignes) {
            productionApi.vendre(
                    ligne.getProduitId(),
                    ligne.getQuantite());
        }
    }

    private void retournerConsommationBoutique(List<LigneRetourRequestDto> retours){
        for (LigneRetourRequestDto retour : retours) {
            productionApi.retourner(retour.getProduitId(), retour.getQuantite());
        }
    }

    private void verifierDisponibilite(List<LigneVenteBoutique> lignes) {

        for (LigneVenteBoutique ligne : lignes) {
            productionApi.verifierDisponibiliteBoutique(ligne.getProduitId(), ligne.getQuantite());
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