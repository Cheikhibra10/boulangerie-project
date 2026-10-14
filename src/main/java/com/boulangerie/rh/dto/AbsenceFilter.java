package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.*;

import java.time.LocalDate;

public record AbsenceFilter(
        String employeNom,
        TypeAbsence type,
        StatutAbsence statut,
        LocalDate dateDebut,
        LocalDate dateFin
) {
}
