package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.comptabilite.exception.CaisseDejaOuverteException;
import com.boulangerie.comptabilite.exception.CaisseFermeeException;
import com.boulangerie.comptabilite.exception.SaisiesIncompletesException;
import com.boulangerie.comptabilite.mapper.CaisseMapper;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.repository.CaisseRepository;
import com.boulangerie.comptabilite.service.CaisseClotureService;
import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CaisseServiceImpl implements CaisseService {

    private final CaisseRepository caisseRepository;
    private final CaisseMapper caisseMapper;
    private final CaisseClotureService clotureService;
    private final MouvementCaisseService mouvementService;
    private final CurrentUserService currentUserService;

    @Override
    public CaisseDto ouvrirCaisse(OuvertureCaisseDto dto) {
        LocalDate today = LocalDate.now();
        try {
            Utilisateur currentUser = currentUserService.getCurrentUser();
            Caisse caisse = new Caisse()
                    .setDateOuverture(Instant.now())
                    .setSoldeInitial(dto.getSoldeInitial())
                    .setStatut(StatutCaisse.OUVERTE)
                    .setOuvertePar(currentUser);

            caisse = caisseRepository.save(caisse);
            log.info("Caisse ouverte par {} avec solde initial {} FCFA", currentUserService.getCurrentUser().getNom(), dto.getSoldeInitial());
            return caisseMapper.toDto(caisse);
        } catch (DataIntegrityViolationException e) {
            throw new CaisseDejaOuverteException();
        }
    }

    @Override
    public CaisseDto fermerCaisse(Long caisseId, FermetureCaisseDto dto) {
        Caisse caisse = caisseRepository.findById(caisseId)
                .orElseThrow(() -> new EntityNotFoundException("Caisse introuvable" +caisseId));

        if (caisse.getStatut() == StatutCaisse.FERMEE) {
            throw new CaisseFermeeException();
        }
        // Vérifier que toutes les saisies sont complètes (RG04)
        if (!clotureService.verifierSaisiesCompletes(caisseId)) {
            throw new SaisiesIncompletesException();
        }
        // Calculer le solde théorique
        BigDecimal totalEntrees = mouvementService.calculerTotalEntrees(caisseId);
        BigDecimal totalSorties = mouvementService.calculerTotalSorties(caisseId);

        if (totalEntrees == null) totalEntrees = BigDecimal.ZERO;
        if (totalSorties == null) totalSorties = BigDecimal.ZERO;

        BigDecimal soldeFinal = caisse.getSoldeInitial().add(totalEntrees).subtract(totalSorties);

        Utilisateur currentUser = currentUserService.getCurrentUser();
        // Fermer la caisse
        caisse.setDateFermeture(Instant.now())
                .setSoldeFinal(soldeFinal)
                .setSoldePhysique(dto.getSoldePhysique())
                .setStatut(StatutCaisse.FERMEE)
                .setFermeePar(currentUser);
        caisse = caisseRepository.save(caisse);
        if (caisse.getEcart().compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Écart de caisse de {} FCFA pour la caisse {} (fermée par {})",
                    caisse.getEcart(), caisseId, currentUser.getPrenom() + " " + currentUser.getNom());
        }

        return caisseMapper.toDto(caisse);
    }
    @Override
    @Transactional(readOnly = true)
    public CaisseDto getCaisseEnCours() {
        return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
                .map(caisseMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Caisse ouverte introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public CaisseDto getCaisse(Long id) {
        return caisseRepository.findById(id)
                .map(caisseMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Caisse introuvable" +id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CaisseDto> getCaisses(int page, int size) {
        Page<Caisse> pageResult = caisseRepository.findAll(PageRequest.of(page, size));
        return PageUtils.toPageResponse(pageResult.map(caisseMapper::toDto));
    }

    @Override
    public MouvementCaisseDto enregistrerPaiement(Long caisseId, BigDecimal montant, String libelle, TypePaiement modePaiement) {
        Caisse caisse = findCaisseOrThrow(caisseId);
        verifierCaisseOuverte(caisse);
        return mouvementService.enregistrerPaiement(caisseId,montant,libelle,modePaiement);
    }

    @Override
    public MouvementCaisseDto enregistrerPaiementAbonnement(Long caisseId, BigDecimal montant, String libelle, TypePaiement modePaiement) {
        return mouvementService.enregistrerPaiementAbonnement(caisseId,montant,libelle,modePaiement);
    }

    @Override
    @Transactional
    public MouvementCaisseDto enregistrerDepense(Long caisseId, Long categorieId, BigDecimal montant, String libelle) {
        verifierCaisseOuverte(findCaisseOrThrow(caisseId));
        return mouvementService.enregistrerDepense(caisseId,categorieId,montant,libelle);
    }

    @Override
    public MouvementCaisseDto enregistrerVersementLivreur(Long caisseId, Long livreurId, BigDecimal montant, TypePaiement modePaiement) {
        if (livreurId == null) {
            throw new BadRequestException("Le livreur est obligatoire");
        }
        return mouvementService.enregistrerVersementLivreur(caisseId, livreurId, montant, modePaiement);
    }


    public void verifierCaisseOuverte(Caisse caisse) {
        if (caisse.getStatut() != StatutCaisse.OUVERTE) {
            throw new CaisseFermeeException();
        }
    }
    @Override
    @Transactional(readOnly = true)
    public JournalCaisseDto getJournal(Long caisseId, LocalDate dateDebut, LocalDate dateFin, String type, Long categorieId, int page, int size) {
        // On utilise directement le service de recherche de mouvements
        PageResponse<MouvementCaisseDto> mouvementsPage = mouvementService.rechercher(
                caisseId, dateDebut, dateFin, type, categorieId, null, page, size
        );
        // Calcul des totaux
        BigDecimal totalEntrees = mouvementService.calculerTotalEntrees(caisseId);
        BigDecimal totalSorties = mouvementService.calculerTotalSorties(caisseId);

        if (totalEntrees == null) totalEntrees = BigDecimal.ZERO;
        if (totalSorties == null) totalSorties = BigDecimal.ZERO;

        Caisse caisse = caisseRepository.findById(caisseId)
                .orElseThrow(() -> new EntityNotFoundException("Caisse introuvable" +caisseId));
        BigDecimal soldeTheorique = caisse.getSoldeInitial().add(totalEntrees).subtract(totalSorties);

        return JournalCaisseDto.builder()
                .mouvements(mouvementsPage.getContent())
                .totalEntrees(totalEntrees)
                .totalSorties(totalSorties)
                .soldeTheorique(soldeTheorique)
                .totalElements(mouvementsPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Override
    public boolean isCaisseOuverte(Long id) {
        return false;
    }

    @Override
    public Caisse getCaisseOuverte() {
        return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
                .orElseThrow(() -> new EntityNotFoundException("Caisse ouverte introuvable"));
    }

    @Override
    public Long getCaisseOuverteOrThrow(Long id) {
        if(!caisseRepository.existsByStatut(StatutCaisse.OUVERTE)){
            throw new EntityNotFoundException("Caisse ouverte introuvable");
        }
        return id;
    }

    @Override
    public Caisse findCaisseOrThrow(Long id) {
        return caisseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caisse introuvable" +id));
    }

}