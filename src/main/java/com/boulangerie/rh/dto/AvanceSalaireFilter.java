package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutAvance;

import java.time.LocalDate;

public record AvanceSalaireFilter(
        String employeNom,
        StatutAvance statut,
        LocalDate dateDebut,
        LocalDate dateFin
) {
}
