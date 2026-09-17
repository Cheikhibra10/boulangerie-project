package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;
import com.boulangerie.comptabilite.service.VersementsCsvExportService;
import com.boulangerie.comptabilite.service.VersementsExcelExportService;
import com.boulangerie.comptabilite.service.VersementsReportingService;
import com.boulangerie.production.api.ProductionMensuelleReportDto;
import com.boulangerie.production.service.ProductionReportingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/comptabilites")
@RequiredArgsConstructor
@Tag(name = "Comptabilite", description = "Gestion des compta ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ComptabiliteController {

    private final VersementsReportingService versementsReportingService;
    private final VersementsExcelExportService excelExportService;
    private final VersementsCsvExportService csvExportService;

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

    @GetMapping(
            value = "/mensuel/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterToutesConsommationsMensuelles(
            @DateTimeFormat(pattern = "MM-yyyy")
            @RequestParam YearMonth periode
    ) {
        VersementsRapportMensuelDto report =
                versementsReportingService.genererRapportMensuel(
                        periode
                );

        byte[] fichier =
                excelExportService.exporterVersementsMensuel(
                        report
                );

        return excelResponse(
                fichier,
                "versements-" + periode.getMonth() + "-" +periode.getYear() + ".xlsx"
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

    @GetMapping(value = "/mensuel/csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterConsommationMensuelleCsv(
            @RequestParam
            @DateTimeFormat(pattern = "MM-yyyy")
            YearMonth periode
    ) {
        VersementsRapportMensuelDto report = versementsReportingService.genererRapportMensuel(periode);

        byte[] fichier = csvExportService.exporterVersementsMensuel(report);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"consommation-"
                        + periode
                        + ".csv\""
        ).contentType(MediaType.parseMediaType("text/csv")
        ).body(fichier);
    }
}
