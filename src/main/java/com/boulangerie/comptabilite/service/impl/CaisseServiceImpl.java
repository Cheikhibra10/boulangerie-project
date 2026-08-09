package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.comptabilite.exception.AucuneCaisseOuverteException;
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
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CaisseServiceImpl implements CaisseService {

    private final CaisseRepository caisseRepository;
    private final CaisseMapper caisseMapper;
    private final CaisseClotureService clotureService;
    private final MouvementCaisseService mouvementService;
    private final CurrentUserService currentUserService;

    @Override
    public CaisseDto ouvrirCaisse(OuvertureCaisseDto dto) {
        if (caisseRepository.findByStatut(StatutCaisse.OUVERTE).isPresent()) {
            throw new CaisseDejaOuverteException();
        }
        Utilisateur currentUser = currentUserService.getCurrentUser();
        try {
            Caisse caisse = Caisse.ouvrir(currentUser, dto.getSoldeInitial());

            caisse = caisseRepository.save(caisse);

            log.info(
                    "Caisse ouverte par {} avec un solde initial de {} FCFA",
                    currentUser.getNom(),
                    dto.getSoldeInitial()
            );
            return caisseMapper.toDto(caisse);
        } catch (DataIntegrityViolationException e) {
            throw new CaisseDejaOuverteException();
        }
    }

    @Override
    public CaisseDto fermerCaisse(Long caisseId, FermetureCaisseDto dto) {

        Caisse caisse = findCaisseOrThrow(caisseId);

        clotureService.verifierSaisiesCompletes(caisseId);

        BigDecimal totalEntrees = mouvementService.calculerTotalEntrees(caisseId);
        BigDecimal totalSorties = mouvementService.calculerTotalSorties(caisseId);

        Utilisateur currentUser = currentUserService.getCurrentUser();

        BigDecimal soldeTheorique =
                caisse.calculerSoldeTheorique(totalEntrees, totalSorties);

        caisse.fermer(
                soldeTheorique,
                dto.getSoldePhysique(),
                dto.getMotifEcart(),
                currentUser
        );

        caisseRepository.save(caisse);

        if (caisse.hasEcart()) {
            log.warn(
                    "Écart de caisse de {} FCFA pour la caisse {} (fermée par {})",
                    caisse.getEcart(),
                    caisseId,
                    currentUser.getPrenom() + " " + currentUser.getNom()
            );
        }

        return caisseMapper.toDto(caisse);
    }

    @Override
    @Transactional(readOnly = true)
    public CaisseDto getCaisseEnCours() {

        return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
                .map(caisseMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Caisse ouverte introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public CaisseDto getCaisse(Long id) {

        return caisseRepository.findById(id)
                .map(caisseMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Caisse introuvable " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CaisseDto> getCaisses(int page, int size) {

        Page<Caisse> result =
                caisseRepository.findAll(PageRequest.of(page, size));

        return PageUtils.toPageResponse(result.map(caisseMapper::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public JournalCaisseDto getJournal(
            Long caisseId,
            LocalDate dateDebut,
            LocalDate dateFin,
            String type,
            Long categorieId,
            int page,
            int size) {

        PageResponse<MouvementCaisseDto> mouvements =
                mouvementService.rechercher(
                        caisseId,
                        dateDebut,
                        dateFin,
                        type,
                        categorieId,
                        null,
                        page,
                        size
                );

        BigDecimal totalEntrees =
                mouvementService.calculerTotalEntrees(caisseId);

        BigDecimal totalSorties =
                mouvementService.calculerTotalSorties(caisseId);

        Caisse caisse = findCaisseOrThrow(caisseId);

        BigDecimal soldeTheorique =
                caisse.calculerSoldeTheorique(totalEntrees, totalSorties);

        return JournalCaisseDto.builder()
                .mouvements(mouvements.getContent())
                .totalEntrees(totalEntrees)
                .totalSorties(totalSorties)
                .soldeTheorique(soldeTheorique)
                .totalElements(mouvements.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Caisse getCaisseOuverte() {

        return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
                .orElseThrow(AucuneCaisseOuverteException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Caisse findCaisseOrThrow(Long id) {

        return caisseRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Caisse introuvable " + id));
    }
}