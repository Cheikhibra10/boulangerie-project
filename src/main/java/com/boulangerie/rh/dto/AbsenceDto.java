package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.*;

import java.time.LocalDate;

// AbsenceDto.java
public record AbsenceDto(
        Long id,
        Long employeId,
        String employeNomComplet,
        TypeAbsence type,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutAbsence statut,
        String motif
) {}

