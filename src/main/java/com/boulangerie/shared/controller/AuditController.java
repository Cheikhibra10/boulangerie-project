package com.boulangerie.shared.controller;

import com.boulangerie.shared.dto.AuditLogDto;
import com.boulangerie.shared.model.AuditAction;
import com.boulangerie.shared.service.AuditLogService;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Journal d'audit transversal : qui a créé/modifié/supprimé quel
 * enregistrement, quand. Alimenté automatiquement pour toute entité
 * héritant d'AbstractAuditingEntity, sans configuration
 * supplémentaire par module — cf. AuditLogEntityListener.
 *
 * Réservé aux administrateurs : ce journal expose potentiellement
 * l'activité de tous les utilisateurs sur toutes les données de
 * l'application, un niveau de sensibilité au moins comparable aux
 * autres endpoints déjà restreints à ADMIN dans GenericCrudController.
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit", description = "Journal d'audit transversal")
public class AuditController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Rechercher dans le journal d'audit")
    @GetMapping
    public ResponseEntity<PageResponse<AuditLogDto>> rechercher(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                auditLogService.rechercher(
                        entityName,
                        entityId,
                        action,
                        performedBy,
                        debut,
                        fin,
                        page,
                        size
                )
        );
    }

    @Operation(
            summary = "Historique complet d'un enregistrement précis"
    )
    @GetMapping("/{entityName}/{entityId}")
    public ResponseEntity<PageResponse<AuditLogDto>> historique(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
                auditLogService.historique(
                        entityName,
                        entityId,
                        page,
                        size
                )
        );
    }
}