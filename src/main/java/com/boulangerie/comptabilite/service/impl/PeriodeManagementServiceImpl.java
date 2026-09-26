package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.dto.ComptabiliteStatistiquesDto;
import com.boulangerie.comptabilite.dto.CreationPeriodeDto;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.exception.PeriodeOverlapException;
import com.boulangerie.comptabilite.mapper.PeriodeMapper;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.comptabilite.repository.ResultatPeriodeRepository;
import com.boulangerie.comptabilite.service.PeriodeManagementService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PeriodeManagementServiceImpl  implements PeriodeManagementService {

    private final PeriodeRepository periodeRepository;
    private final ResultatPeriodeRepository resultatRepository;
    private final PeriodeMapper periodeMapper;

    @Override
    public PeriodeDto creerPeriode(CreationPeriodeDto dto) {
        return enregistrerNouvellePeriode(Periode.creer(
                dto.getDateDebut(),
                dto.getDateFin()
        ));
    }

    @Override
    public PeriodeDto creerPeriodeMensuelle(YearMonth mois) {
        if (mois == null) {
            throw new IllegalArgumentException("Le mois est obligatoire.");
        }

        return enregistrerNouvellePeriode(Periode.creerPourMois(mois));
    }

    @Override
    public PeriodeDto fermerPeriode(Long id) {

        Periode periode = findPeriodeOrThrow(id);

        periode.fermer();

        return periodeMapper.toDto(periodeRepository.save(periode));
    }

    @Override
    public PeriodeDto ouvrirPeriode(Long id) {

        Periode periode = findPeriodeOrThrow(id);

        periode.rouvrir();

        return periodeMapper.toDto(
                periodeRepository.save(periode));
    }

    @Transactional(readOnly = true)
    @Override
    public PeriodeDto getPeriode(Long id) {

        return periodeMapper.toDto(
                findPeriodeOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public PeriodeDto getPeriodeOuverte() {

        return periodeRepository.findByStatut(StatutPeriode.OUVERTE)
                .map(periodeMapper::toDto)
                .orElseThrow(() -> new BusinessException("Aucune période ouverte."));
    }

    @Override
    public Periode findPeriodeOuverte() {
        return periodeRepository.findByStatut(StatutPeriode.OUVERTE)
                .orElseThrow(()-> new EntityNotFoundException("Periode ouverte introuvable: "));

    }

    @Transactional(readOnly = true)
    @Override
    public PeriodeDto getPeriodeByDate(LocalDate date) {

        return periodeRepository.findByDate(date)
                .map(periodeMapper::toDto)
                .orElseThrow(() ->
                        new BusinessException(
                                "Aucune période pour la date " + date));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<PeriodeDto> getPeriodes(int page, int size) {

        Page<Periode> result = periodeRepository.findAllByOrderByDateDebutDesc(
                        PageRequest.of(page, size));

        return PageUtils.toPageResponse(result.map(periodeMapper::toDto));
    }

    @Transactional(readOnly = true)
    @Override
    public ComptabiliteStatistiquesDto getStatistiques() {

        long totalPeriodes = periodeRepository.count();

        long ouvertes = periodeRepository.countByStatut(StatutPeriode.OUVERTE);

        long fermees = periodeRepository.countByStatut(StatutPeriode.FERMEE);

        long cloturees = periodeRepository.countByStatut(StatutPeriode.CLOTUREE);

        long totalResultats = resultatRepository.count();

        BigDecimal caTotal = Optional.ofNullable(resultatRepository.sumCaTotal())
                        .orElse(BigDecimal.ZERO);

        BigDecimal benefice = Optional.ofNullable(resultatRepository.sumBeneficeNet())
                        .orElse(BigDecimal.ZERO);

        BigDecimal beneficeMoyen = totalResultats == 0 ? BigDecimal.ZERO
                : benefice.divide(BigDecimal.valueOf(totalResultats),
                        2,
                        RoundingMode.HALF_UP);

        return ComptabiliteStatistiquesDto.builder()
                .totalPeriodes(totalPeriodes)
                .periodesOuvertes(ouvertes)
                .periodesFermees(fermees)
                .periodesCloturees(cloturees)
                .totalResultats(totalResultats)
                .caTotal(caTotal)
                .beneficeTotal(benefice)
                .beneficeMoyen(beneficeMoyen)
                .dernierePeriodeCloturee(
                        periodeRepository
                                .findTopByStatutOrderByDateFinDesc(
                                        StatutPeriode.CLOTUREE)
                                .map(periodeMapper::toDto)
                                .orElse(null))
                .periodeEnCours(
                        periodeRepository
                                .findByStatut(StatutPeriode.OUVERTE)
                                .map(periodeMapper::toDto)
                                .orElse(null))
                .build();
    }

    private Periode findPeriodeOrThrow(Long id) {

        return periodeRepository.findById(id)
                .orElseThrow(() ->
                        new PeriodeNotFoundException(id));
    }


    private PeriodeDto enregistrerNouvellePeriode(Periode periode) {
        verifierChevauchement(periode);

        reporterBeneficePeriodePrecedente(periode);
        periodeRepository.save(periode);

        log.info("Nouvelle période créée : {} -> {}", periode.getDateDebut(), periode.getDateFin());
        return periodeMapper.toDto(periode);
    }

    private void verifierChevauchement(Periode periode) {

        if (periodeRepository.existsOverlappingPeriods(
                periode.getDateDebut(),
                periode.getDateFin())) {

            throw new PeriodeOverlapException(
                    periode.getDateDebut(),
                    periode.getDateFin());
        }
    }

    private void reporterBeneficePeriodePrecedente(Periode periode) {

        periodeRepository
                .findByDate(periode.getDateDebut().minusDays(1))
                .filter(Periode::estCloturee)
                .map(Periode::getResultat)
                .map(ResultatPeriode::getMontantAReporter)
                .ifPresent(periode::initialiserBeneficeReport);
    }
}
