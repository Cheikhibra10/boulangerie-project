package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.exception.PeriodeNonCloturableException;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.exception.ResultatDejaGenereException;
import com.boulangerie.comptabilite.mapper.PeriodeMapper;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.model.ResultatData;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.comptabilite.repository.ResultatPeriodeRepository;
import com.boulangerie.comptabilite.utils.FinancialConstants;
import com.boulangerie.stocks.api.SnapshotServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodeClosureService {
    
    private final PeriodeRepository periodeRepository;
    private final ResultatPeriodeRepository resultatRepository;
    private final MouvementCaisseService mouvementCaisseService;
    private final PeriodeMapper periodeMapper;
    private final DataAggregationService aggregationService;
    private final ResultatCalculator resultatCalculator;
    private final SnapshotServiceApi snapshotServiceApi;

    @Transactional(rollbackFor = Exception.class)
    public PeriodeDto cloturerPeriode(Long periodeId) {
        // 1. Load and validate period
        Periode periode = periodeRepository.findById(periodeId)
                .orElseThrow(() -> new PeriodeNotFoundException(periodeId));
        if (!periode.estOuverte()) {
            throw new PeriodeNonCloturableException(periodeId, periode.getStatut());
        }
        if (periode.hasResultat()) {
            throw new ResultatDejaGenereException(periodeId);
        }
        // 2. Aggregate all data
        DataAggregationService.AggregatedData aggregated = aggregationService.agregerDonnees(periode);
        // 3. Calculate result
        ResultatData resultatData = resultatCalculator.calculer(
                periode,
                aggregated.getCaAbonnements(),
                aggregated.getCaVentesLivreurs(),
                aggregated.getCaVentesBoutique(),
                aggregated.getCaVenteRestants(),
                aggregated.getCaAutresProduits(),
                aggregated.getTotalCharges(),
                aggregated.getReliquatLivreurs(),
                aggregated.getCreditsClients()
        );
        // 4. Create result
        ResultatPeriode resultat = ResultatPeriode.creer(periode, resultatData);
        periode.attacherResultat(resultat);
        resultatRepository.save(resultat);
        // 5. Close period
        periode.cloturer();
        // 6. Report benefit if any
        BigDecimal beneficeNet = resultatData.getBeneficeNet();
        if (beneficeNet.compareTo(FinancialConstants.MIN_BENEFIT_THRESHOLD) > 0) {
            periode.reporterBenefice(beneficeNet);
            mouvementCaisseService.creerMouvementReportBenefice(periodeId, beneficeNet);
        }
        // 7. Create stock snapshots
        snapshotServiceApi.creerSnapshots(periodeId);
        // 8. Save everything
         periode = periodeRepository.save(periode);
        log.info("Période {} clôturée avec succès. CA: {}, Bénéfice: {}", periodeId, resultatData.getCaTotal(), beneficeNet);
        return periodeMapper.toDto(periode);
    }

}