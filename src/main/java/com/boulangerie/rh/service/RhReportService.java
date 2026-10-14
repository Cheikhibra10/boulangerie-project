package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.AbsencesReportDto;
import com.boulangerie.rh.dto.MasseSalarialeReportDto;

import java.time.LocalDate;

public interface RhReportService {

    MasseSalarialeReportDto masseSalariale(Long periodeId);

    AbsencesReportDto absences(LocalDate dateDebut, LocalDate dateFin);
}