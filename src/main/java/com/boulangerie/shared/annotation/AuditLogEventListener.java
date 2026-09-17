package com.boulangerie.shared.annotation;

import com.boulangerie.shared.model.*;
import com.boulangerie.shared.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Persiste réellement l'AuditLog, APRÈS que la transaction
 * d'origine (celle qui a créé/modifié/supprimé l'entité auditée)
 * ait commité avec succès. Si cette transaction est annulée, aucun
 * AuditLog n'est écrit — cohérent, puisque l'opération elle-même
 * n'a jamais eu lieu.
 *
 * Tourne dans sa PROPRE transaction (REQUIRES_NEW), en dehors de
 * celle d'origine qui est de toute façon déjà terminée à ce stade.
 */
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogEventListener.class);

    private final AuditLogRepository auditLogRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {

        try {

            AuditLog auditLog = AuditLog.builder()
                    .entityName(event.entityName())
                    .entityId(event.entityId())
                    .action(event.action())
                    .performedBy(event.performedBy())
                    .performedAt(event.performedAt())
                    .snapshot(event.snapshot())
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {

            /*
             * Une écriture d'audit ratée ne doit jamais se
             * propager : l'opération métier d'origine est déjà
             * commitée à ce stade, il n'y a rien à annuler.
             */
            log.warn(
                    "Échec d'écriture de l'audit log pour {} #{} : {}",
                    event.entityName(),
                    event.entityId(),
                    e.getMessage()
            );
        }
    }
}