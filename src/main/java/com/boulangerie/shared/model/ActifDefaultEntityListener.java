package com.boulangerie.shared.model;

import jakarta.persistence.PrePersist;

public class ActifDefaultEntityListener {
    @PrePersist
    public void appliquerActifParDefaut(Object entity) {
        if (entity instanceof Activable activable && activable.getActif() == null) {
            activable.setActif(true);
        }
    }
}