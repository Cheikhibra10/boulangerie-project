package com.boulangerie.rh.dto;

import java.time.LocalDate;
import java.util.List;

public record AbsencesReportDto(
        LocalDate dateDebut,
        LocalDate dateFin,
        long nombreAbsences,
        long joursAbsentsTotal,
        List<AbsenceParEmployeDto> parEmploye
) {
}
