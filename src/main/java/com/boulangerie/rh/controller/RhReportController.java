package com.boulangerie.rh.controller;

import com.boulangerie.rh.dto.AbsencesReportDto;
import com.boulangerie.rh.dto.MasseSalarialeReportDto;
import com.boulangerie.rh.service.RhReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rh/reports")
@RequiredArgsConstructor
@Tag(name = "Rapports RH", description = "Masse salariale et synthèse des absences")
public class RhReportController {

    private final RhReportService rhReportService;

    @GetMapping("/masse-salariale")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Masse salariale d'une période",
            description = "Agrège les bulletins VALIDE et PAYE de la période comptable."
    )
    @ApiResponse(responseCode = "200", description = "Rapport généré")
    public MasseSalarialeReportDto masseSalariale(
            @Parameter(description = "ID de la période comptable", required = true)
            @RequestParam Long periodeId
    ) {
        return rhReportService.masseSalariale(periodeId);
    }

    @GetMapping("/absences")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Synthèse des absences approuvées",
            description = "Total et détail par employé sur la plage de dates."
    )
    @ApiResponse(responseCode = "200", description = "Rapport généré")
    public AbsencesReportDto absences(
            @Parameter(required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @Parameter(required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) {
        return rhReportService.absences(dateDebut, dateFin);
    }
}