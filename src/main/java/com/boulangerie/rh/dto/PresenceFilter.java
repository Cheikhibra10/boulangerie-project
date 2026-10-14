package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutPresence;

import java.time.LocalDate;

public record PresenceFilter(
        String employeNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutPresence statut
) {
}
