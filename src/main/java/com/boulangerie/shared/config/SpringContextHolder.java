package com.boulangerie.shared.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * JPA instancie les classes @EntityListeners lui-même (via
 * Hibernate), PAS Spring — donc @Autowired ne fonctionne pas
 * directement dedans. C'est le pont standard pour leur donner
 * accès au contexte Spring (ici : pour publier un événement via
 * ApplicationEventPublisher depuis AuditLogEntityListener).
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        if (context == null) {
            throw new IllegalStateException(
                    "ApplicationContext n'est pas encore initialisé"
            );
        }

        return context;
    }

    public static <T> T getBean(Class<T> type) {
        return getContext().getBean(type);
    }
}