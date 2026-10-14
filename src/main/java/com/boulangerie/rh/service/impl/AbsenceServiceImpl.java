package com.boulangerie.rh.service.impl;

import com.boulangerie.rh.mapper.AbsenceMapper;
import com.boulangerie.rh.model.*;
import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.repository.AbsenceRepository;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.service.AbsenceService;
import com.boulangerie.rh.specification.AbsenceSpecifications;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbsenceServiceImpl implements AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final EmployeRepository employeRepository;
    private final AbsenceMapper absenceMapper;

    @Override
    public AbsenceDto create(CreateAbsenceRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable"));

        Absence absence = absenceMapper.toEntity(request, employe);
        absence.setStatut(StatutAbsence.EN_ATTENTE);
        absence.valider();

        return absenceMapper.toDto(absenceRepository.save(absence));
    }

    @Transactional(readOnly = true)
    @Override
    public AbsenceDto getById(Long id) {
        return absenceMapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AbsenceDto> search(AbsenceFilter filter, Pageable pageable) {
        List<Long> employeIds = null;

        if (filter.employeNom() != null && !filter.employeNom().isBlank()) {
            employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(filter.employeNom());
            if (employeIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        return absenceRepository
                .findAll(AbsenceSpecifications.withFilters(filter, employeIds), pageable)
                .map(absenceMapper::toDto);
    }

    @Override
    public AbsenceDto approuver(Long id) {
        Absence absence = findOrThrow(id);
        absence.approuver();
        return absenceMapper.toDto(absence);
    }

    @Override
    public AbsenceDto refuser(Long id, String motif) {
        Absence absence = findOrThrow(id);
        absence.refuser(motif);
        return absenceMapper.toDto(absence);
    }

    @Override
    public AbsenceDto annuler(Long id) {
        Absence absence = findOrThrow(id);
        absence.annuler();
        return absenceMapper.toDto(absence);
    }

    private Absence findOrThrow(Long id) {
        return absenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Absence introuvable: " + id));
    }
}