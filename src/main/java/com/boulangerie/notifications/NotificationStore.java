package com.boulangerie.notifications;

import com.boulangerie.notifications.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationStore {

    void add(Notification notification);

    List<Notification> findAll(boolean unreadOnly);

    long countUnread();

    Optional<Notification> markAsRead(String id);

    void markAllAsRead();

    void clear();
}