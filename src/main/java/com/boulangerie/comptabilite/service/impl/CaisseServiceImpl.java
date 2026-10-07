package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.comptabilite.dto.*;
import com.boulangerie.comptabilite.exception.AucuneCaisseOuverteException;
import com.boulangerie.comptabilite.exception.CaisseDejaOuverteException;
import com.boulangerie.comptabilite.mapper.CaisseMapper;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.comptabilite.repository.CaisseRepository;
import com.boulangerie.comptabilite.service.CaisseClotureService;
import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.comptabilite.specification.CaisseSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @Transactional(readOnly = true)
    @Override
    public JournalCaisseDto getJournal(
            Long caisseId,
            LocalDate dateDebut,
            LocalDate dateFin,
            TypeMouvement type,
            SensMouvement sens,
            String libelle,
            int page,
            int size
    ) {
        MouvementCaisseFilter filter = new MouvementCaisseFilter(
                type,
                sens,
                libelle,
                dateDebut,
                dateFin,
                null // caisseStatut not needed when filtering by caisseId
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<MouvementCaisseDto> mouvements =
                mouvementService.searchByCaisse(caisseId, filter, pageable);

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

    @Transactional(readOnly = true)
    public Page<CaisseDto> search(CaisseFilter filter, Pageable pageable) {
        return caisseRepository
                .findAll(CaisseSpecifications.withFilters(filter), pageable)
                .map(caisseMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 1) {
            return List.of();
        }

        // Search by id (numeric) or by statut name
        Specification<Caisse> spec;

        try {
            Long id = Long.parseLong(q.trim());
            spec = (root, query, cb) -> cb.equal(root.get("id"), id);
        } catch (NumberFormatException e) {
            // Try matching statut
            spec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("statut").as(String.class)),
                            "%" + q.toLowerCase().trim() + "%");
        }

        return caisseRepository
                .findAll(spec, PageRequest.of(0, 15))
                .stream()
                .map(c -> AutocompleteItemDto.of(
                        c.getId(),
                        "Caisse #" + c.getId(),
                        c.getStatut() != null ? c.getStatut().name() : null
                ))
                .toList();
    }
}
