package com.boulangerie.notifications;

import com.boulangerie.notifications.model.Notification;
import com.boulangerie.shared.model.Notifiable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private static final String EVENT_SUFFIX = "Event";

    private final NotificationStore store;
    private final NotificationBroadcaster broadcaster;

    public NotificationEventListener(NotificationStore store, NotificationBroadcaster broadcaster) {
        this.store = store;
        this.broadcaster = broadcaster;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotifiable(Notifiable event) {
        Notification notification = new Notification(
                deriveType(event),
                event.libelle(),
                event.montant()
        );

        store.add(notification);
        broadcaster.broadcast(notification);
    }

    private String deriveType(Notifiable event) {
        String simpleName = event.getClass().getSimpleName();
        return simpleName.endsWith(EVENT_SUFFIX)
                ? simpleName.substring(0, simpleName.length() - EVENT_SUFFIX.length())
                : simpleName;
    }
}