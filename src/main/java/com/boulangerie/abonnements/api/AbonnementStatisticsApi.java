package com.boulangerie.abonnements.api;

import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.projection.ConsommationReportProjection;
import com.boulangerie.abonnements.projection.LigneAbonnementReportProjection;
import com.boulangerie.abonnements.projection.PaiementReportProjection;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface AbonnementStatisticsApi {

    BigDecimal calculerCA(LocalDate debut, LocalDate fin);
    Integer calculerNActif();
    BigDecimal calculerCC();
    Abonnement getAbonnement(Long id);
    List<Abonnement> findAbonnementsActifsPourPeriode(LocalDate debut, LocalDate fin);
    List<ConsommationReportProjection> getReportData(
            List<Long> abonnementIds,
            LocalDate debut,
            LocalDate fin
    );
    List<ConsommationReportProjection> getReportData(
            Long abonnementId,
            LocalDate debut,
            LocalDate fin
    );

    List<LigneAbonnementReportProjection> getReportData(Long abonnementIds);
    List<LigneAbonnementReportProjection> getReportData(List<Long> abonnementIds);

    List<PaiementReportProjection> getReportData(
            Long abonnementId,
             Instant debut,
             Instant fin
    );
    List<PaiementReportProjection> getReportData(
            List<Long> abonnementId,
            Instant debut,
            Instant fin
    );
}