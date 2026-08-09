package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.administration.service.CategorieDepenseService;
import com.boulangerie.comptabilite.dto.*;
import com.boulangerie.comptabilite.mapper.DepenseMapper;
import com.boulangerie.comptabilite.model.*;
import com.boulangerie.comptabilite.repository.DepensePeriodeRepository;
import com.boulangerie.comptabilite.service.*;
import com.boulangerie.comptabilite.specification.DepenseSpecification;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.shared.dto.MouvementCaisseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DepenseServiceImpl implements DepenseService {

    private final DepensePeriodeRepository depenseRepository;
    private final DepenseMapper depenseMapper;
    private final ApplicationEventPublisher publisher;
    private final CaisseService caisseService;
    private final MouvementCaisseService mouvementService;
    private final CategorieDepenseService categorieService;
    private final PeriodeManagementService periodeService;

    @Override
    public DepenseDto enregistrerDepense(EnregistrerDepenseDto dto) {

        Caisse caisse = caisseService.getCaisseOuverte();

        CategorieDepense categorie = categorieService.findCategorieDepenseOrThrow(dto.getCategorieId());

        Periode periode = periodeService.findPeriodeOuverte();

        MouvementCaisse mouvement = mouvementService.creerMouvement(
                        TypeMouvement.DEPENSE_PERIODE,
                        SensMouvement.SORTIE,
                        TypePaiement.CASH,
                        caisse,
                        dto.getLibelle(),
                        dto.getMontant()
                );

        DepensePeriode depense = new DepensePeriode()
                .setCategorie(categorie)
                .setPeriode(periode)
                .setMouvement(mouvement);

        depense = depenseRepository.save(depense);
        publisher.publishEvent(
                new MouvementCaisseEvent(
                        caisse.getId(),
                        TypeMouvement.DEPENSE_PERIODE,
                        SensMouvement.SORTIE,
                        TypePaiement.CASH,
                        dto.getMontant(),
                        "Depense  #" + dto.getLibelle()
                )
        );
        return depenseMapper.toDto(depense);
    }


    @Override
    @Transactional(readOnly = true)
    public DepenseDto getDepense(Long id) {

        DepensePeriode depense =
                depenseRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Dépense introuvable " + id));

        return depenseMapper.toDto(depense);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<DepenseDto> getDepenses(
            Long categorieId,
            Long periodeId,
            int page,
            int size) {

        Specification<DepensePeriode> specification =
                Specification.where(
                                DepenseSpecification.categorie(categorieId))
                        .and(
                                DepenseSpecification.periode(periodeId));

        Page<DepensePeriode> result =
                depenseRepository.findAll(
                        specification,
                        PageRequest.of(page, size));

        return PageUtils.toPageResponse(
                result.map(depenseMapper::toDto));
    }
}