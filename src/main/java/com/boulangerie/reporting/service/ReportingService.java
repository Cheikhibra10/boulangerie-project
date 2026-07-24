// reporting/application/service/ReportingService.java
package com.boulangerie.reporting.service;

import com.boulangerie.reporting.dto.*;

import java.time.LocalDate;

public interface ReportingService {

    /**
     * Récupère les indicateurs clés de performance pour une période donnée
     */
    KpiDto getKPIs(LocalDate dateDebut, LocalDate dateFin);

    /**
     * Récupère le tableau de bord complet pour une période donnée
     */
    DashboardDto getDashboard(LocalDate dateDebut, LocalDate dateFin);
}