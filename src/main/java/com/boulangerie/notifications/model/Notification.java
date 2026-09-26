package com.boulangerie.notifications.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Notification {

    private final String id;
    private final String type;
    private final String message;
    private final BigDecimal montant;
    private final Instant createdAt;
    private volatile boolean read;

    public Notification(String type, String message, BigDecimal montant) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.message = message;
        this.montant = montant;
        this.createdAt = Instant.now();
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void markAsRead() {
        this.read = true;
    }
}