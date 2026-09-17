package com.boulangerie.shared.model;

import java.time.Instant;

/**
 * Événement publié par AuditLogEntityListener depuis un callback
 * JPA. Ne porte QUE des valeurs déjà calculées et détachées de
 * Hibernate (String/Long/enum/Instant) — jamais l'entité elle-même
 * ni une référence à sa session — pour que le listener qui
 * persistera réellement l'AuditLog (après commit, potentiellement
 * dans une session Hibernate différente) n'ait aucun risque de
 * toucher un état périmé ou une collection non chargée.
 */
public record AuditEvent(

        String entityName,

        Long entityId,

        AuditAction action,

        String performedBy,

        Instant performedAt,

        String snapshot

) {
}