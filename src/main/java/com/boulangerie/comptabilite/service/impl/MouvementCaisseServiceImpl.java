package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.model.*;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.mapper.MouvementCaisseMapper;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.comptabilite.specification.MouvementCaisseSpecification;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.*;
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
    private final MouvementCaisseMapper mouvementMapper;

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


    @Transactional
    @Override
    public MouvementCaisse creerMouvement(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            String libelle,
            BigDecimal montant)
    {
        return repository.save(MouvementCaisse.creer(type, sens, modePaiement, caisse, libelle, montant));
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