package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.AggregatedData;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.mapper.PeriodeMapper;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.model.ResultatData;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.shared.dto.MouvementCaisseEvent;
import com.boulangerie.shared.model.*;
import com.boulangerie.stocks.api.SnapshotServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodeClosureService {
    
    private final PeriodeRepository periodeRepository;
    private final CaisseService caisseService;
    private final ApplicationEventPublisher publisher;
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

        publierBeneficeSiNecessaire(periode);


        snapshotServiceApi.creerSnapshots(id);

        periode.cloturer();

        periodeRepository.save(periode);

        return periodeMapper.toDto(periode);
    }

    private void publierBeneficeSiNecessaire(Periode periode) {

        BigDecimal benefice = periode.getBeneficeReport();

        if (benefice == null || benefice.signum() <= 0) {
            return;
        }

        publisher.publishEvent(
                new MouvementCaisseEvent(
                        caisseService.getCaisseOuverte().getId(),
                        TypeMouvement.REPORT_BENEFICE,
                        SensMouvement.ENTREE,
                        TypePaiement.CASH,
                        benefice,
                        "Report bénéfice période #" + periode.getId()
                )
        );
    }

    private Periode findPeriode(Long id) {
        return periodeRepository.findById(id)
                .orElseThrow(() -> new PeriodeNotFoundException(id));
    }

}