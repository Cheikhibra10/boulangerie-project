package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.AggregatedData;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.mapper.PeriodeMapper;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.model.ResultatData;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.stocks.api.SnapshotServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodeClosureService {
    
    private final PeriodeRepository periodeRepository;
    private final PeriodeMapper periodeMapper;
    private final DataAggregationService aggregationService;
    private final ResultatCalculator resultatCalculator;
    private final SnapshotServiceApi snapshotServiceApi;

    @Transactional
    public PeriodeDto cloturerPeriode(Long id) {
        Periode periode = findPeriode(id);
        periode.verifierCloturable();
        AggregatedData donnees = aggregationService.agregerDonnees(periode);

        ResultatData resultatData = resultatCalculator.calculer(periode, donnees);

        ResultatPeriode resultat = ResultatPeriode.creer(periode, resultatData);

        periode.enregistrerResultat(resultat);

        reporterBeneficeDansPeriodeSuivante(periode, resultat);

        snapshotServiceApi.creerSnapshots(id);

        periode.cloturer();

        periodeRepository.save(periode);

        return periodeMapper.toDto(periode);
    }

    private void reporterBeneficeDansPeriodeSuivante(
            Periode periode,
            ResultatPeriode resultat) {

        periodeRepository.findByDate(periode.getDateFin().plusDays(1))
                .ifPresent(periodeSuivante -> {
                    periodeSuivante.initialiserBeneficeReport(
                            resultat.getMontantAReporter());
                    periodeRepository.save(periodeSuivante);
                });
    }

    private Periode findPeriode(Long id) {
        return periodeRepository.findById(id)
                .orElseThrow(() -> new PeriodeNotFoundException(id));
    }

}
