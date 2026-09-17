package com.boulangerie.shared.service;

import com.boulangerie.shared.dto.AuditLogDto;
import com.boulangerie.shared.model.AuditAction;
import com.boulangerie.shared.dto.PageResponse;

import java.time.Instant;

public interface AuditLogService {

    PageResponse<AuditLogDto> rechercher(
            String entityName,
            Long entityId,
            AuditAction action,
            String performedBy,
            Instant debut,
            Instant fin,
            int page,
            int size
    );

    /**
     * Historique complet (le plus récent en premier) d'un
     * enregistrement précis, tous types d'action confondus.
     */
    PageResponse<AuditLogDto> historique(
            String entityName,
            Long entityId,
            int page,
            int size
    );
}