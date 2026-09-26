package com.boulangerie.notifications.dto;

import com.boulangerie.notifications.model.Notification;

import java.math.BigDecimal;
import java.time.Instant;

public record NotificationDto(
        String id,
        String type,
        String message,
        BigDecimal montant,
        Instant createdAt,
        boolean read
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getMontant(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}