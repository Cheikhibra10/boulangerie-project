// reporting/application/service/ReportingApplicationService.java
package com.boulangerie.reporting.service.impl;

import com.boulangerie.abonnements.dto.AbonnementConsommationReportDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.reporting.dto.*;
import com.boulangerie.reporting.service.KpiCalculator;
import com.boulangerie.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingServiceImpl implements ReportingService {

    private final KpiCalculator kpiCalculator;

    @Override
    public KpiDto getKPIs(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Génération des KPIs pour la période {} - {}", dateDebut, dateFin);
        return kpiCalculator.calculerKPIs(dateDebut, dateFin);
    }

    @Override
    public DashboardDto getDashboard(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Génération du tableau de bord pour la période {} - {}", dateDebut, dateFin);

        KpiDto kpis = kpiCalculator.calculerKPIs(dateDebut, dateFin);
        List<TopProduitDto> topProduits = kpiCalculator.getTopProduits(dateDebut, dateFin, 5);
        List<EvolutionVenteDto> evolution = kpiCalculator.getEvolutionVentes(dateDebut, dateFin);
        AchatStatistiquesDto achats = kpiCalculator.getStatistiques();

        return new  DashboardDto()
                .setKpis(kpis)
                .setTopProduits(topProduits)
                .setEvolutionVentes(evolution)
                .setStatutsAchats(achats);
    }

//    private ConsommationMensuelleReportDto construireRapportGlobal(
//            YearMonth periode,
//            List<AbonnementConsommationReportDto> abonnements
//    ) {
//        ConsommationMensuelleReportDto report =
//                new ConsommationMensuelleReportDto()
//                        .setPeriode(periode)
//                        .setAbonnements(
//                                new ArrayList<>(abonnements)
//                        );
//
//        for (AbonnementConsommationReportDto abonnement : abonnements) {
//
//            report.setQuantiteTotale(
//                    report.getQuantiteTotale()
//                            .add(
//                                    abonnement.getQuantiteTotale()
//                            )
//            );
//
//            report.setMontantMensuelTotal(
//                    report.getMontantMensuelTotal()
//                            .add(
//                                    abonnement.getMontantMensuelTotal()
//                            )
//            );
//
//            report.setMontantVerseTotal(
//                    report.getMontantVerseTotal()
//                            .add(
//                                    abonnement.getMontantVerseTotal()
//                            )
//            );
//
//            report.setReliquatTotal(
//                    report.getReliquatTotal()
//                            .add(
//                                    abonnement.getReliquatTotal()
//                            )
//            );
//        }
//
//        return report;
//    }
}