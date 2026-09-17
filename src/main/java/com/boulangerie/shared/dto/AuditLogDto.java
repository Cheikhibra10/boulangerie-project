package com.boulangerie.shared.dto;

import com.boulangerie.shared.model.AuditAction;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;

    private String entityName;

    private Long entityId;

    private AuditAction action;

    private String performedBy;

    private Instant performedAt;

    private JsonNode snapshot;
}