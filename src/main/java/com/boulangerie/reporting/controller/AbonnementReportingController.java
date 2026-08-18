package com.boulangerie.reporting.controller;

import com.boulangerie.abonnements.api.ConsommationMensuelleReportDto;
import com.boulangerie.abonnements.service.AbonnementReportingService;
import com.boulangerie.reporting.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reporting")
@RequiredArgsConstructor
public class AbonnementReportingController {

    private final AbonnementReportingService abonnementReportingService;
    private final ExcelExportService excelExportService;


    @GetMapping(
            value = "/abonnements/{abonnementId}/consommations/mensuel/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterConsommationMensuelle(
            @PathVariable Long abonnementId,
            @RequestParam YearMonth periode
    ) {

        ConsommationMensuelleReportDto report =
                abonnementReportingService.genererRapportMensuel(
                        abonnementId,
                        periode
                );

        byte[] fichier =
                excelExportService.exporterConsommationMensuelle(
                        report
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"consommation-"
                                + abonnementId
                                + "-"
                                + periode
                                + ".xlsx\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(fichier);
    }

    @GetMapping(
            value = "/abonnements/consommations/mensuel/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterToutesConsommationsMensuelles(
            @RequestParam YearMonth periode
    ) {

        ConsommationMensuelleReportDto report =
                abonnementReportingService.genererRapportMensuel(
                        periode
                );

        byte[] fichier =
                excelExportService.exporterConsommationMensuelle(
                        report
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"consommations-"
                                + periode
                                + ".xlsx\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(fichier);
    }
}