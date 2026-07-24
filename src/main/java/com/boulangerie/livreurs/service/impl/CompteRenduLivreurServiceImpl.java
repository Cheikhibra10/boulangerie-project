package com.boulangerie.livreurs.service.impl;

import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.livreurs.dto.*;
import com.boulangerie.livreurs.mapper.CompteRenduMapper;
import com.boulangerie.livreurs.mapper.LigneCompteRenduMapper;
import com.boulangerie.livreurs.model.*;
import com.boulangerie.livreurs.repository.CompteLivreurJournalierRepository;
import com.boulangerie.livreurs.service.*;
import com.boulangerie.production.api.AllocationDetails;
import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
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
public class CompteRenduLivreurServiceImpl implements CompteRenduLivreurService {

    private final CompteLivreurJournalierRepository journalierRepository;
    private final LivreurService livreurService;
    private final ProductionAllocationApi productionApi;
    private final CompteLivreurJournalierFactory journalierFactory;
    private final CommissionCalculator commissionCalculator;
    private final VersementLivreurService versementLivreurService;
    private final CompteLivreurService compteLivreurService;

    private final CompteRenduMapper compteRenduMapper;
    private final LigneCompteRenduMapper ligneMapper;

    private final CurrentUserService currentUserService;

    @Override
    public CompteRenduDto creerOuRecupererCompteRendu(CompteRenduCreationDto dto) {

        Long livreur = livreurService.findLivreurOrThrow(dto.getLivreurId());
//        verifierDestinationsDisponibles(livreur.getId(), dto.getDate());

        CompteLivreurJournalier journalier = journalierRepository.findByLivreurIdAndDate(livreur, dto.getDate())
                        .orElseGet(() -> journalierFactory.create(livreur, dto.getDate()));
        journalier.verifierNonCloture();
        ajouterLignes(journalier, dto.getLignes());

        log.info("Compte rendu {} préparé avec {} lignes", journalier.getId(), journalier.getLignes().size());
        return compteRenduMapper.toDto(journalier);
    }

    @Override
    public CompteRenduDto cloturerCompteRendu(Long journalierId, ClotureCompteRenduDto dto) {

        CompteLivreurJournalier journalier = findJournalierOrThrow(journalierId);
        journalier.verifierNonCloture();
        VersementLivreur versement = versementLivreurService.creerVersement(journalier, dto);
        journalier.cloturer(versement);
        compteLivreurService.mettreAJourSolde(journalier.getLivreurId(), journalier.getReliquatFin());
        log.info("Compte rendu={} livreur={} total={} reliquat={} clôturé par={}",
                journalier.getId(),
                journalier.getLivreurId(),
                journalier.getTotalAVerser(),
                journalier.getReliquatFin(),
                currentUserService.getCurrentUser().getNom());
        return compteRenduMapper.toDto(journalier);
    }

    @Override
    @Transactional(readOnly = true)
    public CompteRenduDto getCompteRendu(Long id) {

        return compteRenduMapper.toDto(findJournalierOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CompteRenduDto> getHistorique(Long livreurId, LocalDate debut, LocalDate fin, int page, int size) {

        Page<CompteLivreurJournalier> result;
        if (debut != null && fin != null) {
            result = journalierRepository.findByLivreurIdAndDateBetween(livreurId, debut, fin, PageRequest.of(page, size));
        } else {
            result = journalierRepository.findByLivreurIdOrderByDateDesc(livreurId, PageRequest.of(page, size));
        }

        return PageUtils.toPageResponse(result.map(compteRenduMapper::toDto));
    }

    private void ajouterLignes(CompteLivreurJournalier journalier, List<LigneCompteRenduRequestDto> lignes) {

        for (LigneCompteRenduRequestDto dto : lignes) {
            AllocationDetails allocation = productionApi.getAllocation(dto.getDestinationProductionId());
            verifierAllocation(journalier, allocation, dto);
            LigneCompteLivreur ligne = ligneMapper.toEntity(dto);

            BigDecimal prixCommission = commissionCalculator.calculate(
                            journalier.getLivreurId(),
                            allocation.produitId(),
                            journalier.getDate());

            if (dto.getQteLivree().compareTo(allocation.quantite()) > 0) {
                throw new BadRequestException("Quantité supérieure à l'allocation");
            }
            ligne.initialiser(allocation.prixUnitaire(), prixCommission, allocation.destinationId());
            journalier.addLigne(ligne);
        }
    }

    private CompteLivreurJournalier findJournalierOrThrow(Long id) {
        return journalierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compte rendu introuvable : " + id));
    }

    private void verifierAllocation(
            CompteLivreurJournalier journalier,
            AllocationDetails allocation,
            LigneCompteRenduRequestDto dto) {

        if (!allocation.livreurId().equals(journalier.getLivreurId())) {
            throw new BadRequestException(
                    "Cette destination n'appartient pas au livreur.");
        }

        if (dto.getQteLivree().compareTo(allocation.quantite()) > 0) {
            throw new BadRequestException(
                    "Quantité supérieure à l'allocation.");
        }
    }

}