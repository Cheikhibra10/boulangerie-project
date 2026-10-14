package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutPresence;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreatePresenceRequest(
        @NotNull Long employeId,
        @NotNull LocalDate date,
        @NotNull StatutPresence statut,
        LocalTime heureArrivee,
        LocalTime heureDepart
) {
}
