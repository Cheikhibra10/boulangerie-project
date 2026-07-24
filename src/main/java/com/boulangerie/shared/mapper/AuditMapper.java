package com.boulangerie.shared.mapper;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.service.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditMapper {

    private final UserDirectoryService userDirectoryService;

    public void enrich(
            AbstractAuditingEntity entity,
            AbstractAuditingDto dto
    ) {

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setCreatedBy(
                userDirectoryService.getUser(
                        entity.getCreatedBy()
                ).fullName()
        );

        dto.setUpdatedBy(
                userDirectoryService.getUser(
                        entity.getUpdatedBy()
                ).fullName()
        );
    }
}