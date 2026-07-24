// caisse/service/impl/MouvementCaisseServiceImpl.java
package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.administration.service.CategorieDepenseService;
import com.boulangerie.comptabilite.exception.CaisseFermeeException;
import com.boulangerie.comptabilite.model.*;
import com.boulangerie.comptabilite.repository.CaisseRepository;
import com.boulangerie.comptabilite.service.PeriodeService;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.mapper.MouvementCaisseMapper;
import com.boulangerie.comptabilite.repository.DepensePeriodeRepository;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.comptabilite.specification.MouvementCaisseSpecification;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.model.TypePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MouvementCaisseServiceImpl implements MouvementCaisseService {

    private final MouvementCaisseRepository repository;
    private final CaisseRepository caisseRepository;
    private final PeriodeService periodeService;
    private final MouvementCaisseMapper mouvementMapper;
    private final CategorieDepenseService categorieDepenseService;
    private final DepensePeriodeRepository depensePeriodeRepository;

    @Override
    @Transactional
    public MouvementCaisseDto enregistrerPaiement(
            Long caisseId,
            BigDecimal montant,
            String libelle,
            TypePaiement modePaiement) {
        Caisse caisse = findCaisseOrThrow(caisseId);
        verifierCaisseOuverte(caisse);
        return mouvementMapper.toDto(
                creerMouvement(
                        TypeMouvement.PAIEMENT,
                        SensMouvement.ENTREE,
                        modePaiement,
                        caisse,
                        libelle,
                        montant
                )
        );
    }

    private void verifierCaisseOuverte(Caisse caisse) {
        if (caisse.getStatut() != StatutCaisse.OUVERTE) {
            throw new CaisseFermeeException();
        }
    }


    @Override
    @Transactional
    public MouvementCaisseDto enregistrerPaiementAbonnement(
            Long caisseId,
            BigDecimal montant,
            String libelle,
            TypePaiement modePaiement) {
        Caisse caisse = findCaisseOrThrow(caisseId);
        return mouvementMapper.toDto(
                creerMouvement(
                        TypeMouvement.PAIEMENT_ABONNEMENT,
                        SensMouvement.ENTREE,
                        modePaiement,
                        caisse,
                        libelle,
                        montant
                )
        );
    }


    @Override
    @Transactional
    public MouvementCaisseDto enregistrerVersementLivreur(
            Long caisseId,
            Long livreurId,
            BigDecimal montant,
            TypePaiement modePaiement) {
        Caisse caisse = findCaisseOrThrow(caisseId);
        return mouvementMapper.toDto(
                creerMouvement(
                        TypeMouvement.VERSEMENT_LIVREUR,
                        SensMouvement.ENTREE,
                        modePaiement,
                        caisse,
                        "Versement livreur #" + livreurId,
                        montant
                )
        );
    }

    @Transactional
    @Override
    public MouvementCaisseDto enregistrerDepense(
            Long caisseId,
            Long categorieId,
            BigDecimal montant,
            String libelle) {

        Caisse caisse = findCaisseOrThrow(caisseId);
        CategorieDepense categorie = categorieDepenseService.findCategorieDepenseOrThrow(categorieId);
        Periode periode = periodeService.getPeriodeOuverte();

        MouvementCaisse mouvement = creerMouvement(
                TypeMouvement.DEPENSE_PERIODE,
                SensMouvement.SORTIE,
                TypePaiement.CASH,
                caisse,
                libelle,
                montant
        );

        depensePeriodeRepository.save(
                new DepensePeriode()
                        .setPeriode(periode)
                        .setCategorie(categorie)
                        .setMouvement(mouvement)
        );

        return mouvementMapper.toDto(mouvement);
    }

    @Transactional
    @Override
    public void creerMouvementReportBenefice(Long periode, BigDecimal montant) {
        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setTypeMouvement(TypeMouvement.REPORT_BENEFICE);
        mouvement.setSens(SensMouvement.ENTREE);
        mouvement.setMontant(montant);
        mouvement.setModePaiement(TypePaiement.CASH);
        mouvement.setLibelle("Report bénéfice période ");
        repository.save(mouvement);
    }

    @Override
    @Transactional
    public void enregistrerPaiementFournisseur(BigDecimal montant, TypePaiement modePaiement, String libelle, Instant date) {
        Caisse caisse = getCaisseOuverte();

        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setMontant(montant);
        mouvement.setModePaiement(modePaiement);
        mouvement.setLibelle(libelle);
        mouvement.setTypeMouvement(TypeMouvement.PAIEMENT_FOURNISSEUR);
        mouvement.setSens(SensMouvement.SORTIE);
        mouvement.setCaisse(caisse);
        repository.save(mouvement);
    }

    private Caisse findCaisseOrThrow(Long id) {
        return caisseRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Caisse introuvable : " + id));
    }

    private Caisse getCaisseOuverte() {
        return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
                .orElseThrow(CaisseFermeeException::new);
    }

    private MouvementCaisse creerMouvement(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            String libelle,
            BigDecimal montant) {
        verifierMontant(montant);
        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setTypeMouvement(type);
        mouvement.setSens(sens);
        mouvement.setModePaiement(modePaiement);
        mouvement.setMontant(montant);
        mouvement.setLibelle(libelle);
        mouvement.setCaisse(caisse);
        return repository.save(mouvement);
    }


    @Override
    @Transactional
    public MouvementCaisse creerMouvementCaisse(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            Periode periode,
            String libelle,
            BigDecimal montant
    ) {
        verifierMontant(montant);

        MouvementCaisse mouvement =  new MouvementCaisse();
        mouvement.setMontant(montant);
        mouvement.setModePaiement(modePaiement);
        mouvement.setTypeMouvement(TypeMouvement.PAIEMENT_ABONNEMENT);
        mouvement.setSens(SensMouvement.ENTREE);
        mouvement.setLibelle("Paiement abonnement");
        mouvement.setCaisse(caisse);

        return repository.save(mouvement);
    }

    private void verifierMontant(BigDecimal montant){
        if(montant == null || montant.compareTo(BigDecimal.ZERO) <= 0){
            throw new BadRequestException("Le montant doit être positif");
        }

    }



    @Override
    @Transactional(readOnly = true)
    public PageResponse<MouvementCaisseDto> rechercher(
            Long caisseId,
            LocalDate dateDebut,
            LocalDate dateFin,
            String type,
            Long categorieId,
            Long livreurId,
            int page,
            int size) {

        Instant start = dateDebut != null
                ? dateDebut.atStartOfDay(ZoneId.systemDefault()).toInstant()
                : null;
        Instant end = dateFin != null
                ? dateFin.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
                : null;

        TypeMouvement typeEnum = type != null ? TypeMouvement.valueOf(type) : null;

        Specification<MouvementCaisse> spec = Specification
                .where(MouvementCaisseSpecification.byCaisse(caisseId))
                .and(MouvementCaisseSpecification.dateBetween(start, end))
                .and(MouvementCaisseSpecification.byType(typeEnum))
                .and(MouvementCaisseSpecification.byCategorieDepense(categorieId))
                .and(MouvementCaisseSpecification.byLivreur(livreurId));

        Page<MouvementCaisse> pageResult = repository.findAll(spec, PageRequest.of(page, size));
        Page<MouvementCaisseDto> dtoPage = pageResult.map(mouvementMapper::toDto);
        return toPageResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculerTotalEntrees(Long caisseId) {
        return Optional.ofNullable(
                        repository.sumMontantByCaisseIdAndSens(
                                caisseId,
                                SensMouvement.ENTREE))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculerTotalSorties(Long caisseId) {
        return Optional.ofNullable(
                repository.sumMontantByCaisseIdAndSens(
                        caisseId,
                        SensMouvement.SORTIE))
                .orElse(BigDecimal.ZERO);
    }

    protected PageResponse<MouvementCaisseDto> toPageResponse(Page<MouvementCaisseDto> page) {
        return PageResponse.<MouvementCaisseDto>builder()
                .content(page.getContent())
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}