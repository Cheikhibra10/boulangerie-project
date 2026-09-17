package com.boulangerie.shared.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Traçage OPÉRATIONNEL (logs SLF4J, pour le débogage et le suivi
 * des exceptions) — DISTINCT du journal d'audit métier
 * (com.boulangerie.audit, table audit_logs, "qui a créé/modifié/
 * supprimé quel enregistrement"). Les deux ne doivent pas se
 * mélanger : celui-ci ne persiste rien et n'a aucune valeur de
 * preuve/conformité, il sert uniquement à comprendre ce qui se
 * passe pendant l'exécution.
 *
 * Inspiré de l'aspect de logging par défaut de JHipster, adapté au
 * découpage par domaine de ce projet (pas de séparation
 * repository/service/web.rest à la racine — chaque module a ses
 * propres sous-packages).
 *
 * Différence volontaire par rapport à la version JHipster de
 * référence : logAround ne fait plus AUCUNE gestion d'exception —
 * il proceed() et laisse l'exception remonter. Dans l'original,
 * logAround rattrapait spécifiquement IllegalArgumentException
 * pour la logger, alors que logAfterThrowing logue déjà TOUTE
 * exception séparément — ça double-loguait exactement le même
 * événement. Ici, chaque advice a une seule responsabilité :
 * logAround trace entrée/sortie, logAfterThrowing trace les
 * exceptions. Une seule fois, un seul endroit.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final String PROFIL_DEV = "dev";

    private final Environment env;

    public LoggingAspect(Environment env) {
        this.env = env;
    }

    /**
     * Beans Spring standards : Repository, Service, RestController.
     */
    @Pointcut(
            "within(@org.springframework.stereotype.Repository *)"
                    + " || within(@org.springframework.stereotype.Service *)"
                    + " || within(@org.springframework.web.bind."
                    + "annotation.RestController *)"
    )
    public void springBeanPointcut() {
    }

    /**
     * Limite l'interception au code applicatif (pas aux
     * dépendances tierces qui passeraient par les mêmes
     * stéréotypes).
     */
    @Pointcut("within(com.boulangerie..*)")
    public void applicationPackagePointcut() {
    }

    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(
                joinPoint.getSignature().getDeclaringTypeName()
        );
    }

    /**
     * Log des exceptions levées par le code applicatif.
     * En profil dev : stacktrace complète.
     * Sinon (prod) : message réduit, sans stacktrace ni détail
     * potentiellement sensible.
     */
    @AfterThrowing(
            pointcut = "applicationPackagePointcut() "
                    + "&& springBeanPointcut()",
            throwing = "e"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {

        if (env.acceptsProfiles(Profiles.of(PROFIL_DEV))) {

            logger(joinPoint).error(
                    "Exception dans {}() — cause = '{}', "
                            + "message = '{}'",
                    joinPoint.getSignature().getName(),
                    e.getCause() != null ? e.getCause() : "NULL",
                    e.getMessage(),
                    e
            );

        } else {

            logger(joinPoint).error(
                    "Exception dans {}() — cause = {}",
                    joinPoint.getSignature().getName(),
                    e.getCause() != null
                            ? String.valueOf(e.getCause())
                            : "NULL"
            );
        }
    }

    /**
     * Trace entrée/sortie en DEBUG uniquement — silencieux par
     * défaut (DEBUG désactivé en prod), donc sans coût ni bruit
     * hors débogage explicite.
     *
     * ATTENTION : Arrays.toString(joinPoint.getArgs()) appelle
     * toString() sur chaque argument. Si une méthode reçoit
     * directement une entité JPA (plutôt qu'un DTO — rare dans ce
     * projet, mais pas interdit) et que cette entité a un
     * @ToString généré sur un champ @OneToMany/@ManyToOne LAZY,
     * ça peut déclencher un chargement paresseux depuis un contexte
     * inattendu. Aucune entité de ce projet n'utilise @ToString à
     * ce jour (@Getter/@Setter seulement), donc le risque est nul
     * en l'état — à surveiller si ça change.
     */
    @Around(
            "applicationPackagePointcut() && springBeanPointcut()"
    )
    public Object logAround(ProceedingJoinPoint joinPoint)
            throws Throwable {

        Logger log = logger(joinPoint);

        if (log.isDebugEnabled()) {

            log.debug(
                    "Entrée : {}() avec argument(s) = {}",
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs())
            );
        }

        Object result = joinPoint.proceed();

        if (log.isDebugEnabled()) {

            log.debug(
                    "Sortie : {}() avec résultat = {}",
                    joinPoint.getSignature().getName(),
                    result
            );
        }

        return result;
    }
}