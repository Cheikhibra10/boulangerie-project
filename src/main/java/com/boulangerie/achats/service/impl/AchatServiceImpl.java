package com.boulangerie.achats.service.impl;

import com.boulangerie.achats.dto.*;
import com.boulangerie.achats.event.AchatReceptionneEvent;
import com.boulangerie.achats.exception.*;
import com.boulangerie.achats.mapper.*;
import com.boulangerie.achats.model.*;
import com.boulangerie.achats.repository.*;
import com.boulangerie.achats.service.*;
import com.boulangerie.achats.specification.AchatSpecificationBuilder;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AchatServiceImpl implements AchatService {

    private final AchatRepository achatRepository;
    private final LigneAchatRepository ligneRepository;
    private final IngredientRepository ingredientRepository;
    private final ApplicationEventPublisher publisher;
    private final AchatFactory achatFactory;
    private final ReceptionAchatService receptionService;
    private final RetourAchatService retourAchatService;
    private final AchatMapper achatMapper;
    private final LigneAchatMapper ligneMapper;
    private final PaiementFournisseurMapper paiementFournisseurMapper;
    private final CurrentUserService currentUserService;

    @Override
    public AchatDto creerAchat(CreationAchatDto dto) {
        Achat achat = achatFactory.create(dto);
        achat = achatRepository.save(achat);

        log.info("Achat créé par {} : fournisseur={}, ID={}",
                currentUserService.getCurrentUser().getNom(),
                achat.getFournisseur().getNom(),
                achat.getId());

        return achatMapper.toDto(achat);
    }

    @Override
    public LigneAchatDto ajouterLigne(Long achatId, LigneAchatRequestDto dto) {
        Achat achat = findAchatOrThrow(achatId);

        if (!achat.estModifiable()) {
            throw new AchatNonModifiableException(achatId, achat.getStatutReception());
        }

        Ingredient ingredient = findIngredientOrThrow(dto.getIngredientId());

        achat.ajouterLigne(ingredient, dto.getQuantite(), dto.getPrixUnitaire());
        achat = achatRepository.save(achat);

        // Récupérer la ligne créée
        LigneAchat ligne = achat.getLignes().stream()
                .filter(l -> l.getIngredient().equals(ingredient))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ligne non trouvée après ajout"));

        log.info("Ligne ajoutée à l'achat {} : {} unités de {}", achatId, dto.getQuantite(), ingredient.getLibelle());

        return ligneMapper.toDto(ligne);
    }

    @Override
    public LigneAchatDto modifierLigne(Long ligneId, LigneAchatRequestDto dto) {
        LigneAchat ligne = findLigneOrThrow(ligneId);
        Achat achat = ligne.getAchat();

        if (!achat.estModifiable()) {
            throw new AchatNonModifiableException(achat.getId(), achat.getStatutReception());
        }
        if (!ligne.getIngredient().getId().equals(dto.getIngredientId())) {
            throw new BadRequestException("L'ingrédient ne peut pas être modifié. Supprimez et recréez la ligne.");
        }
        achat.modifierLigne(ligneId, dto.getQuantite(), dto.getPrixUnitaire());
        achatRepository.save(achat);

        log.info("Ligne {} modifiée : quantite={}, prix={}", ligneId, dto.getQuantite(), dto.getPrixUnitaire());
        return ligneMapper.toDto(ligne);
    }

    @Override
    public void supprimerLigne(Long ligneId) {
        LigneAchat ligne = findLigneOrThrow(ligneId);
        Achat achat = ligne.getAchat();

        if (!achat.estModifiable()) {
            throw new AchatNonModifiableException(achat.getId(), achat.getStatutReception());
        }

        achat.supprimerLigne(ligneId);
        achatRepository.save(achat);

        log.info("Ligne {} supprimée de l'achat {}", ligneId, achat.getId());
    }

    @Override
    public AchatDto recevoirAchat(Long achatId, ReceptionAchatRequestDto dto) {
        Achat achat = findAchatOrThrow(achatId);
        if (achat.getLignes().isEmpty()) {
            throw new AchatSansLigneException(achatId);
        }

        List<ReceptionLigneDto> receptions = new ArrayList<>();
        for (ReceptionLigneRequest dtoLigne : dto.getLignes()) {
            LigneAchat ligne = achat.getLigne(dtoLigne.getLigneId());
            receptions.add(
                    new ReceptionLigneDto(
                            dtoLigne.getLigneId(),
                            dtoLigne.getQuantiteRecue(),
                            dtoLigne.getQuantiteRefusee(),
                            dtoLigne.getMotifRefus()
                    )
            );
        }
        achat.recevoir(receptions);
        receptionService.recevoirAchat(achat, receptions);
        achat = achatRepository.save(achat);
        log.info("Achat {} réceptionné par {}",
                achatId,
                currentUserService.getCurrentUser().getNom());

        publisher.publishEvent(new AchatReceptionneEvent(
                achat.getId(),
                achat.getFournisseur().getNom(),
                achat.getMontantTotal(),
                "Achat #" + achat.getId() + " réceptionné — " + achat.getFournisseur().getNom()
        ));

        return achatMapper.toDto(achat);
    }

    @Override
    @Transactional
    public AchatDto retournerAchat(Long achatId, RetourAchatRequestDto dto) {

        Achat achat = findAchatOrThrow(achatId);

        List<RetourLigneDto> retours = new ArrayList<>();
        for (RetourLigneRequest dtoLigne : dto.getLignes()) {
            LigneAchat ligne = achat.getLigne(dtoLigne.getLigneId());
            retours.add(
                    new RetourLigneDto(
                            dtoLigne.getLigneId(),
                            dtoLigne.getQuantiteRetournee(),
                            dtoLigne.getMotifRefus()
                    )
            );
        }
        achat.retourner(retours);
        retourAchatService.retourner(achat, retours);
        achat = achatRepository.save(achat);

        log.info("Retour fournisseur achat {}", achatId);
        return achatMapper.toDto(achat);
    }

    @Override
    public PaiementFournisseurDto enregistrerPaiement(Long achatId, PaiementFournisseurRequestDto dto) {
        // 1. Find the achat
        Achat achat = findAchatOrThrow(achatId);
        // 2. Validate achat state
        if (achat.estAnnule()) {
            throw new AchatAnnuleException(achatId);
        }
        if (achat.estPaye()) {
            throw new AchatDejaPayeException(achatId);
        }
        // 3. Validate payment amount (against restant dû)
        BigDecimal restantDu = achat.getRestantDu();
        if (dto.getMontant().compareTo(restantDu) > 0) {
            throw new MontantExcedentaireException(achatId, dto.getMontant(), restantDu);
        }
        if (dto.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontantInvalideException("Le montant du paiement doit être positif");
        }

        // 5. Let aggregate handle the payment (creates paiement and updates state)
        PaiementFournisseur paiement = achat.enregistrerPaiement(dto.getMontant(), dto.getModePaiement());
        // 6. Save the aggregate (cascade saves paiement)
        achat = achatRepository.save(achat);

        // 7. Log and return
        log.info("Paiement enregistré pour l'achat {} : {} FCFA (mode: {})", achatId, dto.getMontant(), dto.getModePaiement());
        return paiementFournisseurMapper.toDto(paiement);
    }

    @Override
    public AchatDto annulerAchat(Long achatId) {
        Achat achat = findAchatOrThrow(achatId);
        if (achat.estPaye()) {
            throw new AchatDejaRecuException(achatId);
        }
        achat.annuler();
        achat = achatRepository.save(achat);
        log.info("Achat {} annulé par {}", achatId, currentUserService.getCurrentUser().getNom());
        return achatMapper.toDto(achat);
    }

    @Override
    @Transactional(readOnly = true)
    public AchatDto getAchat(Long id) {
        return achatRepository.findById(id)
                .map(achatMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Achat introuvable: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LigneAchatDto> getLignesByAchat(Long achatId) {
        return ligneRepository.findByAchatIdWithIngredient(achatId).stream()
                .map(ligneMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<AchatDto> getAchats(AchatSearchRequest request, int page, int size) {

        Specification<Achat> specification = AchatSpecificationBuilder.build(request);

        Page<Achat> achats = achatRepository.findAll(specification, PageRequest.of(page, size));

        return PageUtils.toPageResponse(achats.map(achatMapper::toDto));
    }

    private Achat findAchatOrThrow(Long id) {
        return achatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Achat introuvable: " + id));
    }

    private Ingredient findIngredientOrThrow(Long id) {
        return ingredientRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Ingrédient actif introuvable: " + id));
    }

    private LigneAchat findLigneOrThrow(Long id) {
        return ligneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("LigneAchat introuvable: " + id));
    }
}