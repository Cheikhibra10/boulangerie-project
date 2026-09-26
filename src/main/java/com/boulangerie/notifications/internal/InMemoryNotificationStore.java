package com.boulangerie.notifications.internal;

import com.boulangerie.notifications.NotificationStore;
import com.boulangerie.notifications.model.Notification;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Component
public class InMemoryNotificationStore implements NotificationStore {

    private static final int MAX_NOTIFICATIONS = 500;

    private final Deque<Notification> notifications = new ArrayDeque<>();
    private final Object lock = new Object();

    @Override
    public void add(Notification notification) {
        synchronized (lock) {
            notifications.addFirst(notification);
            while (notifications.size() > MAX_NOTIFICATIONS) {
                notifications.removeLast();
            }
        }
    }

    @Override
    public List<Notification> findAll(boolean unreadOnly) {
        synchronized (lock) {
            return notifications.stream()
                    .filter(n -> !unreadOnly || !n.isRead())
                    .toList();
        }
    }

    @Override
    public long countUnread() {
        synchronized (lock) {
            return notifications.stream().filter(n -> !n.isRead()).count();
        }
    }

    @Override
    public Optional<Notification> markAsRead(String id) {
        synchronized (lock) {
            Optional<Notification> found = notifications.stream()
                    .filter(n -> n.getId().equals(id))
                    .findFirst();
            found.ifPresent(Notification::markAsRead);
            return found;
        }
    }

    @Override
    public void markAllAsRead() {
        synchronized (lock) {
            notifications.forEach(Notification::markAsRead);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            notifications.clear();
        }
    }
}