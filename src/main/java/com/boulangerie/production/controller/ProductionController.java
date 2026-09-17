// production/controller/ProductionController.java
package com.boulangerie.production.controller;

import com.boulangerie.abonnements.dto.ConsommationImportResultDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.production.api.ProductionMensuelleReportDto;
import com.boulangerie.production.dto.*;
import com.boulangerie.production.service.*;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Tag(name = "Production", description = "Gestion des lots de production ADMIN-MANAGER-GESTIONNAIRE_PRODUCTION")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN','GESTIONNAIRE_PRODUCTION')")
public class ProductionController {
    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

    private final ProductionReportingService productionReportingService;
    private final ProductionExcelExportService excelExportService;
    private final ProductionService productionService;
    private final ProductionExecutionService productionExecutionService;
    private final ProductionPlanningService productionPlanningService;
    private final ProductionCsvExportService csvExportService;
    @Operation(summary = "Créer un lot de production")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<ApiResponse<LotProductionDto>> creerProduction(@Valid @RequestBody CreerProductionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lot de production créé", productionPlanningService.creerProduction(dto)));
    }

    @Operation(summary = "Enregistrer la quantité réalisée pour un lot")
    @PatchMapping("/{id}/finalisation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<ApiResponse<LotProductionDto>> finaliserProduction(@PathVariable Long id, @RequestBody FinaliserProductionRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Quantité réalisée enregistrée",
                productionExecutionService.finaliserProduction(id, dto)));
    }

    @Operation(summary = "Répartir la production entre les canaux")
    @PostMapping("/lots/{id}/distribution")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<ApiResponse<List<DestinationDto>>> distribuerProduction(
            @PathVariable Long id,
            @Valid @RequestBody DistribuerProductionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Répartition enregistrée", productionService.distribuerProduction(id, dto)));
    }

    @Operation(summary = "Obtenir un lot par ID")
    @GetMapping("/lots/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<LotProductionDto> getLot(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getLot(id));
    }

    @Operation(summary = "Lister les lots de production (paginé)")
    @GetMapping("/lots")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<PageResponse<LotProductionDto>> getLots(
               @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productionService.getLots(page, size));
    }

    @Operation(summary = "Obtenir les destinations d'un lot")
    @GetMapping("/lots/{id}/destinations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<List<DestinationDto>> getDestinationsByLot(@PathVariable Long id) {
        return ResponseEntity.ok(productionService.getDestinationsByLot(id));
    }

    @Operation(summary = "Obtenir les destinations par livreur et date")
    @GetMapping("/destinations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GESTIONNAIRE_PRODUCTION')")
    public ResponseEntity<List<DestinationDto>> getDestinationsByLivreurEtDate(
            @RequestParam Long livreurId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(productionService.getDestinationsByLivreurEtDate(livreurId, date));
    }

    @GetMapping(
            value = "/mensuel/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exporterToutesConsommationsMensuelles(
            @RequestParam
            @DateTimeFormat(pattern = "MM-yyyy")
            YearMonth periode
    ) {
        ProductionMensuelleReportDto report =
                productionReportingService.genererRapportMensuel(
                        periode
                );

        byte[] fichier =
                excelExportService.exporterProductionMensuelle(
                        report
                );

        return excelResponse(
                fichier,
                "productions-" + periode + ".xlsx"
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
        ProductionMensuelleReportDto report = productionReportingService.genererRapportMensuel(periode);

        byte[] fichier = csvExportService.exporterProductionMensuelle(report);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"consommation-"
                        + periode
                        + ".csv\""
        ).contentType(MediaType.parseMediaType("text/csv")
        ).body(fichier);
    }

}