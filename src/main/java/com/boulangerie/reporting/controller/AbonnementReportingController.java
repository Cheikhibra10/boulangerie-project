package com.boulangerie.reporting.controller;

import com.boulangerie.abonnements.service.AbonnementReportingService;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.reporting.service.CsvExportService;
import com.boulangerie.reporting.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
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

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

    private final AbonnementReportingService abonnementReportingService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;
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

        return excelResponse(
                fichier,
                "consommation-" + abonnementId + "-" + periode + ".xlsx"
        );
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

        return excelResponse(
                fichier,
                "consommations-" + periode + ".xlsx"
        );
    }

    private ResponseEntity<byte[]> excelResponse(
            byte[] fichier,
            String filename
    ) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .attachment()
                                .filename(filename)
                                .build()
                                .toString()
                )
                .contentType(EXCEL_MEDIA_TYPE)
                .contentLength(fichier.length)
                .body(fichier);
    }

    @GetMapping(value = "/abonnements/consommations/mensuel/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterConsommationMensuelleCsv(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth periode
    ) {
        ConsommationMensuelleReportDto report = abonnementReportingService.genererRapportMensuel(periode);

        byte[] fichier = csvExportService.exporterConsommationMensuelle(report);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"consommation-"
                                + periode
                                + ".csv\""
                ).contentType(MediaType.parseMediaType("text/csv")
                ).body(fichier);
    }
}