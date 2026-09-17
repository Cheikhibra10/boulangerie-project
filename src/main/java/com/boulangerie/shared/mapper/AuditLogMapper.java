package com.boulangerie.shared.mapper;

import com.boulangerie.shared.dto.AuditLogDto;
import com.boulangerie.shared.model.AuditLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditLogMapper {

    private final ObjectMapper objectMapper;
    public AuditLogDto toDto(AuditLog auditLog) {

        if (auditLog == null) {
            return null;
        }

        return AuditLogDto.builder()
                .id(auditLog.getId())
                .entityName(auditLog.getEntityName())
                .entityId(auditLog.getEntityId())
                .action(auditLog.getAction())
                .performedBy(auditLog.getPerformedBy())
                .performedAt(auditLog.getPerformedAt())
                .snapshot(toJsonNode(auditLog.getSnapshot()))
                .build();
    }

    private JsonNode toJsonNode(String snapshot) {

        if (snapshot == null || snapshot.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(snapshot);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}