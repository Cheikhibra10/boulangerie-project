package com.boulangerie.shared.mapper;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import org.springframework.stereotype.Component;

/**
 * Copie les champs d'audit (createdAt/updatedAt/createdBy/updatedBy)
 * d'une entité vers son DTO, pour les mappers écrits à la main qui
 * n'ont pas cette copie automatiquement (contrairement à MapStruct,
 * qui la fait déjà tout seul quand les noms de champs correspondent —
 * cf. MouvementCaisseMapper, CaisseMapper).
 *
 * Cette classe était auparavant entièrement commentée et ne
 * compilait pas telle quelle : elle formatait createdAt/updatedAt en
 * String via un DateTimeFormatter puis tentait de les affecter à des
 * champs déclarés en Instant sur AbstractAuditingDto, et résolvait
 * createdBy/updatedBy via UserDirectoryService en supposant qu'il
 * s'agissait d'identifiants opaques à résoudre.
 *
 * Ni l'un ni l'autre n'est nécessaire avec la stratégie d'audit
 * actuelle : SecurityAuditorAware place déjà un nom lisible
 * directement dans created_by/updated_by (prénom+nom, ou le nom
 * d'utilisateur du JWT) — ce ne sont pas des identifiants à
 * résoudre. Et AbstractAuditingDto déclare createdAt/updatedAt en
 * Instant, pas en String : le formatage d'affichage revient au
 * consommateur de l'API, pas à ce mapper.
 */
@Component
public class AuditEnricher {

    public void enrich(
            AbstractAuditingEntity entity,
            AbstractAuditingDto dto
    ) {

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
    }
}