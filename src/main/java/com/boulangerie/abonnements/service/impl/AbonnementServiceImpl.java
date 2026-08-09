// abonnements/service/impl/AbonnementServiceImpl.java
package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.dto.*;
import com.boulangerie.abonnements.event.VersementEvent;
import com.boulangerie.abonnements.exception.AbonnementExpireException;
import com.boulangerie.abonnements.exception.AbonnementInactifException;
import com.boulangerie.abonnements.mapper.AbonnementMapper;
import com.boulangerie.abonnements.mapper.LigneAbonnementMapper;
import com.boulangerie.abonnements.model.*;
import com.boulangerie.abonnements.repository.*;
import com.boulangerie.abonnements.service.*;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.production.api.DistributionService;
import com.boulangerie.shared.dto.MouvementCaisseEvent;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.ConflictException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbonnementServiceImpl implements AbonnementService {


    private final AbonnementRepository abonnementRepository;
    private final LigneAbonnementRepository ligneRepository;
    private final ClientRepository clientRepository;
    private final CompteAbonnementRepository compteRepository;


    private final LivreurService livreurService;

    private final AbonnementFactory abonnementFactory;
    private final ClientFactory clientFactory;

    private final DistributionService distributionService;
    private final AbonnementMapper abonnementMapper;
    private final LigneAbonnementMapper ligneMapper;
    private final PaiementAbonnementService paiementAbonnementService;
    private final VersementGlobalService versementGlobalService;
    private final ApplicationEventPublisher publisher;
    @Override
    @Transactional
    public AbonnementDto creerAbonnement(CreationAbonnementDto dto) {
        Long livreurId = livreurService.findLivreurOrThrow(dto.getLivreurId());

        Abonnement abonnement = abonnementFactory.create(dto, livreurId);
        abonnementRepository.save(abonnement);
        return abonnementMapper.toDto(abonnement);
    }

    @Override
    @Transactional
    public LigneAbonnementDto ajouterClient(Long abonnementId, CreationClientAbonnementDto dto) {
        Abonnement abonnement = findAbonnementOrThrow(abonnementId);
        verifierClientUnique(dto);

        Client client = clientFactory.create(
                        dto.getNom(),
                        dto.getPrenom(),
                        dto.getTelephone()
                );
        clientRepository.save(client);

        LigneAbonnement ligne = abonnement.ajouterClient(client, dto.getPrixUnitaire());
        abonnementRepository.save(abonnement);

        return ligneMapper.toDto(ligne);
    }

    private void verifierClientUnique(CreationClientAbonnementDto dto) {
        if(clientRepository.existsByPrenomAndNomAndTelephone(
                dto.getPrenom(),
                dto.getNom(),
                dto.getTelephone()
         )
                || clientRepository.existsByTelephone(dto.getTelephone())
        )
        {
            throw new ConflictException("Ce client existe déjà.");
        }
    }

    @Override
    @Transactional
    public void enregistrerConsommation(Long ligneId, ConsommationDto dto) {
        LigneAbonnement ligne = findLigneOrThrow(ligneId);

        Abonnement abonnement = ligne.getAbonnement();

        if(!abonnement.estValidePour(dto.getDate())) {
            throw new AbonnementExpireException(abonnement.getId());
        }
        BigDecimal quantiteDistribuee = distributionService.getQuantiteDistribuee(
                        abonnement.getId(),
                        dto.getDate()
                );
        abonnement.verifierQuantiteDisponible(
                dto.getDate(),
                dto.getQuantite(),
                quantiteDistribuee
        );
        ligne.enregistrerConsommation(dto.getDate(), dto.getQuantite());
        ligneRepository.save(ligne);
    }

    @Override
    @Transactional
    public PaiementClientResultDto enregistrerPaiementClient(Long ligneId, PaiementClientDto dto) {

        LigneAbonnement ligne = findLigneOrThrow(ligneId);
        BigDecimal nouveauReliquat = ligne.calculerReliquatApresPaiement(dto.getMontant());
        ligne.payer(dto.getMontant());

        CompteAbonnement compte = ligne.getAbonnement().getCompte();
        compte.crediter(dto.getMontant());

        paiementAbonnementService.enregistrerPaiement(ligne, dto.getMontant(), dto.getModePaiement());
        ligneRepository.save(ligne);
        compteRepository.save(compte);

        return new PaiementClientResultDto()
                .setLigneId(ligneId)
                .setMontantPaye(dto.getMontant())
                .setNouveauReliquat(nouveauReliquat)
                .setEstSolde(nouveauReliquat.compareTo(BigDecimal.ZERO) <= 0);
    }


    @Override
    @Transactional
    public VersementGlobalResultDto enregistrerVersementGlobal(
            Long abonnementId,
            VersementGlobalDto dto) {

        Abonnement abonnement = findAbonnementOrThrow(abonnementId);

        abonnement.verifierActif();

       abonnement.transfererVersBoulangerie(dto.getMontant());

        versementGlobalService.enregistrerVersement(
                abonnement.getCompte(),
                dto.getMontant(),
                dto.getModePaiement()
        );

        return new VersementGlobalResultDto()
                .setAbonnementId(abonnementId)
                .setMontantVerse(dto.getMontant())
                .setNouveauSoldeCompte(abonnement.getCompte().getSoldeActuel());
    }

    @Override
    public AbonnementDto getAbonnement(Long id) {
        return abonnementMapper.toDto(findAbonnementOrThrow(id));
    }

    @Override
    public PageResponse<AbonnementDto> getAbonnements(int page, int size) {

        Page<Abonnement> result = abonnementRepository.findByActifTrue(PageRequest.of(page, size));

        return PageUtils.toPageResponse(result.map(abonnementMapper::toDto)
        );
    }

    private LigneAbonnement findLigneOrThrow(Long ligneId) {

        return ligneRepository.findById(ligneId)
                .orElseThrow(
                        () -> new EntityNotFoundException("LigneAbonnement introuvable: " + ligneId)
                );
    }

    private Abonnement findAbonnementOrThrow(Long abonnementId) {

        return abonnementRepository.findById(abonnementId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Abonnement introuvable: " + abonnementId)
                );
    }
}