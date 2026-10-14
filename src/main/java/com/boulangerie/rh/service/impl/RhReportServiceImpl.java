package com.boulangerie.rh.service.impl;


import com.boulangerie.comptabilite.api.PeriodeLookupApi;
import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.model.StatutAbsence;
import com.boulangerie.rh.model.StatutBulletin;
import com.boulangerie.rh.repository.AbsenceParEmployeProjection;
import com.boulangerie.rh.repository.AbsenceRepository;
import com.boulangerie.rh.repository.BulletinPaieRepository;
import com.boulangerie.rh.repository.MasseSalarialeProjection;
import com.boulangerie.rh.service.RhReportService;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RhReportServiceImpl implements RhReportService {

    private final BulletinPaieRepository bulletinPaieRepository;
    private final AbsenceRepository absenceRepository;
    private final PeriodeLookupApi periodeLookupApi;

    @Override
    public MasseSalarialeReportDto masseSalariale(Long periodeId) {
        requireNonNull(periodeId, "L'identifiant de période est obligatoire.");

        PeriodeLookupApi.PeriodeInfo periode = periodeLookupApi.getByIdOrThrow(periodeId);

        MasseSalarialeProjection aggregate = bulletinPaieRepository.aggregateMasseSalariale(
                periodeId,
                List.of(StatutBulletin.VALIDE, StatutBulletin.PAYE)
        );

        return new MasseSalarialeReportDto(
                periode.id(),
                periode.dateDebut(),
                periode.dateFin(),
                aggregate.getNombre(),
                aggregate.getTotalBrut(),
                aggregate.getTotalRetenues(),
                aggregate.getTotalNet()
        );
    }

    @Override
    public AbsencesReportDto absences(LocalDate dateDebut, LocalDate dateFin) {
        requireNonNull(dateDebut, "La date de début est obligatoire.");
        requireNonNull(dateFin, "La date de fin est obligatoire.");

        if (dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        List<AbsenceParEmployeProjection> projections =
                absenceRepository.aggregateApprouveesByEmploye(StatutAbsence.APPROUVEE, dateDebut, dateFin);

        long totalAbsences = projections.stream()
                .mapToLong(AbsenceParEmployeProjection::getNombreAbsences)
                .sum();

        long totalJours = projections.stream()
                .mapToLong(AbsenceParEmployeProjection::getJoursAbsents)
                .sum();

        List<AbsenceParEmployeDto> parEmploye = projections.stream()
                .map(p -> new AbsenceParEmployeDto(
                        p.getEmployeId(),
                        p.getNomComplet(),
                        p.getNombreAbsences(),
                        p.getJoursAbsents()
                ))
                .toList();

        return new AbsencesReportDto(
                dateDebut,
                dateFin,
                totalAbsences,
                totalJours,
                parEmploye
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new BadRequestException(message);
        }
    }
}