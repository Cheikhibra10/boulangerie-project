package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutPresence;

import java.time.LocalDate;
import java.time.LocalTime;

public record PresenceDto(
        Long id,
        Long employeId,
        String employeNomComplet,
        LocalDate date,
        StatutPresence statut,
        LocalTime heureArrivee,
        LocalTime heureDepart
) {}

