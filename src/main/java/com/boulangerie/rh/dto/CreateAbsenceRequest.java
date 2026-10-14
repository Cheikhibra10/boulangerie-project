package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.TypeAbsence;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAbsenceRequest(
        @NotNull Long employeId,
        @NotNull TypeAbsence type,
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        String motif
) {}