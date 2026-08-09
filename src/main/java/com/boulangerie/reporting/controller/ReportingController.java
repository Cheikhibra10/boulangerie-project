// reporting/api/controller/ReportingController.java
package com.boulangerie.reporting.controller;

import com.boulangerie.reporting.dto.*;
import com.boulangerie.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Tableau de bord et indicateurs clés ADMIN-MANAGER")
public class ReportingController {

    private final ReportingService reportingService;

    @Operation(summary = "Obtenir les KPIs pour une période")
    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<KpiDto> getKPIs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }

        return ResponseEntity.ok(reportingService.getKPIs(dateDebut, dateFin));
    }

    @Operation(summary = "Obtenir le tableau de bord complet")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DashboardDto> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        if (dateDebut == null) {
            dateDebut = LocalDate.now().minusDays(30);
        }
        if (dateFin == null) {
            dateFin = LocalDate.now();
        }
        return ResponseEntity.ok(reportingService.getDashboard(dateDebut, dateFin));
    }
}