package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.model.*;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.mapper.MouvementCaisseMapper;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.comptabilite.specification.MouvementCaisseSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.*;
import com.boulangerie.comptabilite.dto.MouvementCaisseFilter;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
    public BigDecimal calculerTotalEntrees(Long caisseId) {
        return Optional.ofNullable(repository.sumMontantByCaisseIdAndSens(caisseId, SensMouvement.ENTREE))
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
    @Transactional(readOnly = true)
    public Page<MouvementCaisseDto> searchByCaisse(
            Long caisseId,
            MouvementCaisseFilter filter,
            Pageable pageable
    ) {
        Specification<MouvementCaisse> spec = Specification
                .<MouvementCaisse>where((root, query, cb) ->
                        cb.equal(root.get("caisse").get("id"), caisseId))
                .and(MouvementCaisseSpecifications.withFilters(filter));

        return repository
                .findAll(spec, pageable)
                .map(mouvementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<MouvementCaisseDto> search(MouvementCaisseFilter filter, Pageable pageable) {

        Specification<MouvementCaisse> spec = MouvementCaisseSpecifications
                .withFilters(filter);

        return repository
                .findAll(spec, pageable)
                .map(mouvementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        Specification<MouvementCaisse> spec = SearchSpecifications.like("libelle", q);

        return repository
                .findAll(spec, PageRequest.of(0, 15))
                .stream()
                .map(m -> AutocompleteItemDto.of(
                        m.getId(),
                        m.getLibelle() != null ? m.getLibelle() : "Mouvement #" + m.getId(),
                        m.getTypeMouvement() != null ? m.getTypeMouvement().name() : null
                ))
                .toList();
    }

}
