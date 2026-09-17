package com.boulangerie.shared.service.impl;

import com.boulangerie.shared.dto.AuditLogDto;
import com.boulangerie.shared.mapper.AuditLogMapper;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.repository.*;
import com.boulangerie.shared.service.AuditLogService;
import com.boulangerie.shared.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public PageResponse<AuditLogDto> rechercher(
            String entityName,
            Long entityId,
            AuditAction action,
            String performedBy,
            Instant debut,
            Instant fin,
            int page,
            int size
    ) {

        Specification<AuditLog> spec =
                Specification.where(
                                AuditLogSpecification.entityName(entityName)
                        )
                        .and(AuditLogSpecification.entityId(entityId))
                        .and(AuditLogSpecification.action(action))
                        .and(AuditLogSpecification.performedBy(performedBy))
                        .and(
                                AuditLogSpecification.performedAtBetween(
                                        debut,
                                        fin
                                )
                        );

        return executer(spec, page, size);
    }

    @Override
    public PageResponse<AuditLogDto> historique(
            String entityName,
            Long entityId,
            int page,
            int size
    ) {

        Specification<AuditLog> spec =
                Specification.where(
                                AuditLogSpecification.entityName(entityName)
                        )
                        .and(AuditLogSpecification.entityId(entityId));

        return executer(spec, page, size);
    }

    private PageResponse<AuditLogDto> executer(
            Specification<AuditLog> spec,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "performedAt")
        );

        Page<AuditLog> resultat =
                auditLogRepository.findAll(spec, pageable);

        return PageResponse.<AuditLogDto>builder()
                .content(
                        resultat.getContent()
                                .stream()
                                .map(auditLogMapper::toDto)
                                .toList()
                )
                .number(resultat.getNumber())
                .size(resultat.getSize())
                .totalElements(resultat.getTotalElements())
                .totalPages(resultat.getTotalPages())
                .first(resultat.isFirst())
                .last(resultat.isLast())
                .build();
    }
}