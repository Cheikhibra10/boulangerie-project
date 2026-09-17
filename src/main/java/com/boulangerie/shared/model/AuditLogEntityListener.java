package com.boulangerie.shared.model;

import com.boulangerie.shared.config.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enregistré sur AbstractAuditingEntity via @EntityListeners : se
 * déclenche donc pour TOUTE entité qui en hérite, sans rien à
 * ajouter côté entités individuelles.
 *
 * Règles de conception, toutes motivées par un seul principe :
 * l'audit ne doit JAMAIS casser l'opération métier réelle qui l'a
 * déclenché.
 *
 *  1. Tout est enveloppé dans un try/catch qui, au pire, journalise
 *     un avertissement — jamais une exception qui remonte.
 *  2. L'instantané ne capture QUE les champs scalaires (pas les
 *     relations/collections), pour ne jamais déclencher de
 *     chargement paresseux ni de boucle de sérialisation.
 *  3. L'instantané est calculé ICI, pendant que l'entité est encore
 *     attachée à la session — pas plus tard dans le listener
 *     transactionnel qui persistera l'AuditLog après commit,
 *     puisqu'à ce moment la session d'origine peut être fermée.
 *     C'est pour ça qu'AuditEvent ne porte que des valeurs déjà
 *     calculées, jamais l'entité elle-même.
 */
public class AuditLogEntityListener {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogEntityListener.class);

    @PostPersist
    public void onCreate(Object entity) {
        publier(entity, AuditAction.CREATE);
    }

    @PostUpdate
    public void onUpdate(Object entity) {
        publier(entity, AuditAction.UPDATE);
    }

    @PostRemove
    public void onDelete(Object entity) {
        publier(entity, AuditAction.DELETE);
    }

    private void publier(Object entity, AuditAction action) {

        try {

            AuditEvent event = new AuditEvent(
                    entity.getClass().getSimpleName(),
                    extraireId(entity),
                    action,
                    extraireActeur(),
                    Instant.now(),
                    construireInstantane(entity)
            );

            SpringContextHolder
                    .getContext()
                    .publishEvent(event);

        } catch (Exception e) {

            log.warn(
                    "Échec de publication de l'événement d'audit "
                            + "pour {} : {}",
                    entity.getClass().getSimpleName(),
                    e.getMessage()
            );
        }
    }

    private Long extraireId(Object entity) {

        try {

            Object id = entity.getClass().getMethod("getId")
                    .invoke(entity);

            return id instanceof Long longId ? longId : null;

        } catch (Exception e) {
            return null;
        }
    }

    private String extraireActeur() {

        try {

            @SuppressWarnings("unchecked")
            AuditorAware<String> auditorAware =
                    SpringContextHolder.getBean(AuditorAware.class);

            return auditorAware.getCurrentAuditor().orElse(null);

        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Instantané JSON des champs scalaires de l'entité (et de ses
     * superclasses, AbstractAuditingEntity comprise — createdAt/
     * updatedAt/createdBy/updatedBy y figurent donc aussi, ce qui
     * est utile pour tracer l'état exact au moment de l'action).
     * Relations, collections, champs statiques/synthétiques et
     * champs @Transient sont explicitement exclus.
     */
    private String construireInstantane(Object entity) {

        try {

            Map<String, Object> valeurs = new LinkedHashMap<>();

            Class<?> classe = entity.getClass();

            while (classe != null && classe != Object.class) {

                for (Field field : classe.getDeclaredFields()) {

                    if (!estChampSimple(field)) {
                        continue;
                    }

                    field.setAccessible(true);
                    Object valeur = field.get(entity);

                    if (valeur != null) {
                        valeurs.putIfAbsent(field.getName(), valeur);
                    }
                }

                classe = classe.getSuperclass();
            }

            ObjectMapper objectMapper =
                    SpringContextHolder.getBean(ObjectMapper.class);

            return objectMapper.writeValueAsString(valeurs);

        } catch (Exception e) {

            log.warn(
                    "Échec de construction de l'instantané d'audit "
                            + "pour {} : {}",
                    entity.getClass().getSimpleName(),
                    e.getMessage()
            );

            return null;
        }
    }

    private boolean estChampSimple(Field field) {

        if (field.isSynthetic()
                || Modifier.isStatic(field.getModifiers())) {
            return false;
        }

        if (field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(Transient.class)) {
            return false;
        }

        Class<?> type = field.getType();

        return !Collection.class.isAssignableFrom(type)
                && !Map.class.isAssignableFrom(type);
    }
}