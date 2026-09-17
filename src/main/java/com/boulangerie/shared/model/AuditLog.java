package com.boulangerie.shared.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Une entrée du journal d'audit transversal : "qui a fait quoi,
 * quand, sur quel enregistrement".
 *
 * N'étend PAS AbstractAuditingEntity — d'une part pour éviter que
 * son propre listener d'audit se déclenche sur lui-même (boucle),
 * d'autre part parce que performedBy/performedAt jouent déjà ce
 * rôle ici, createdBy/createdAt seraient redondants.
 *
 * Écrite une seule fois, jamais modifiée après coup — c'est un
 * historique, pas un enregistrement métier.
 */
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_logs_entity",
                        columnList = "entity_name,entity_id"
                ),
                @Index(
                        name = "idx_audit_logs_performed_at",
                        columnList = "performed_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom simple de la classe de l'entité auditée (ex:
     * "LotProduction", "MouvementCaisse") — pas le nom qualifié
     * complet, pour rester lisible dans les filtres d'API.
     */
    @Column(name = "entity_name", nullable = false, length = 150)
    private String entityName;

    /**
     * Identifiant de l'entité auditée. Peut être null si
     * l'extraction par réflexion (getId()) a échoué — on ne bloque
     * jamais l'opération métier pour autant, cf.
     * AuditLogEntityListener.
     */
    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    /**
     * Même valeur que ce que SecurityAuditorAware aurait renseigné
     * sur l'entité elle-même (nom lisible, pas un ID opaque) —
     * cohérent avec created_by/updated_by ailleurs dans
     * l'application. Null si l'opération a eu lieu hors contexte de
     * sécurité (ex: job planifié).
     */
    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    /**
     * Instantané JSON des champs SCALAIRES de l'entité au moment de
     * l'opération (pas les relations/collections, pour éviter tout
     * chargement paresseux ou boucle de sérialisation — cf.
     * AuditLogEntityListener.construireInstantane). Peut être null
     * si la sérialisation a échoué ; l'échec ne doit jamais faire
     * échouer l'opération métier auditée.
     *
     * PAS de @Lob ici : avec Hibernate 6 + PostgreSQL, @Lob sur un
     * String fait attendre à Hibernate une colonne de type "oid"
     * (Large Object), pas un simple TEXT — même si la migration
     * crée bien la colonne en TEXT. columnDefinition explicite
     * suffit pour du texte non borné sans déclencher ce mapping.
     */
    @Column(name = "snapshot", columnDefinition = "TEXT")
    private String snapshot;
}