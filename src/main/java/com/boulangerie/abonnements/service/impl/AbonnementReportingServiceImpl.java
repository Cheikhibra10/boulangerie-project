package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.abonnements.dto.ReportingPeriod;
import com.boulangerie.abonnements.service.AbonnementReportingService;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.projection.ConsommationReportProjection;
import com.boulangerie.abonnements.projection.LigneAbonnementReportProjection;
import com.boulangerie.abonnements.projection.PaiementReportProjection;
import com.boulangerie.abonnements.service.ConsommationReportBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AbonnementReportingServiceImpl
        implements AbonnementReportingService {

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Africa/Dakar");

    private final AbonnementStatisticsApi abonnementStatisticsApi;
    private final ConsommationReportBuilder reportBuilder;

    @Override
    public ConsommationMensuelleReportDto genererRapportMensuel(
            Long abonnementId,
            YearMonth periode
    ) {

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        Abonnement abonnement = abonnementStatisticsApi.getAbonnement(abonnementId);
        ReportingPeriod period = ReportingPeriod.of(
                        periode,
                        BUSINESS_ZONE
                );

        List<LigneAbonnementReportProjection> lignes =
                abonnementStatisticsApi.getReportData(abonnementId);

        List<ConsommationReportProjection> consommations =
                abonnementStatisticsApi.getReportData(
                        abonnementId,
                        period.startDate(),
                        period.endDateExclusive()
                );

        List<PaiementReportProjection> paiements =
                abonnementStatisticsApi.getReportData(
                        abonnementId,
                        period.startInstant(),
                        period.endInstant()
                );

        return reportBuilder.build(
                periode,
                List.of(abonnement),
                lignes,
                consommations,
                paiements
        );
    }

    @Override
    public ConsommationMensuelleReportDto genererRapportMensuel(
            YearMonth periode
    ) {

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        ReportingPeriod period = ReportingPeriod.of(
                        periode,
                        BUSINESS_ZONE
                );

        List<Abonnement> abonnements = abonnementStatisticsApi.findAbonnementsActifsPourPeriode(period.startDate(), period.endDateExclusive());

        if (abonnements.isEmpty()) {
            return new ConsommationMensuelleReportDto()
                    .setPeriode(periode);
        }

        List<Long> abonnementIds =
                abonnements.stream()
                        .map(Abonnement::getId)
                        .toList();

        List<LigneAbonnementReportProjection> lignes = abonnementStatisticsApi.getReportData(abonnementIds);

        List<ConsommationReportProjection> consommations =
                abonnementStatisticsApi.getReportData(
                        abonnementIds,
                        period.startDate(),
                        period.endDateExclusive()
                );

        List<PaiementReportProjection> paiements =
                abonnementStatisticsApi.getReportData(
                        abonnementIds,
                        period.startInstant(),
                        period.endInstant()
                );

        return reportBuilder.build(
                periode,
                abonnements,
                lignes,
                consommations,
                paiements
        );
    }
}