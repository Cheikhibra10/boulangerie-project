package com.boulangerie.rh.dto;

public record AbsenceParEmployeDto(
        Long employeId,
        String employeNomComplet,
        long nombreAbsences,
        long joursAbsents
) {
}
