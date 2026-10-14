package com.boulangerie.rh.service.impl;

import com.boulangerie.rh.dto.CreatePresenceRequest;
import com.boulangerie.rh.dto.PresenceDto;
import com.boulangerie.rh.dto.PresenceFilter;
import com.boulangerie.rh.mapper.PresenceMapper;
import com.boulangerie.rh.model.Employe;
import com.boulangerie.rh.model.Presence;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.repository.PresenceRepository;
import com.boulangerie.rh.service.PresenceService;
import com.boulangerie.rh.specification.PresenceSpecifications;
import com.boulangerie.shared.exception.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenceServiceImpl implements PresenceService {

    private final PresenceRepository presenceRepository;
    private final EmployeRepository employeRepository;
    private final PresenceMapper presenceMapper;

    @Override
    public PresenceDto create(CreatePresenceRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable"));

        if (presenceRepository.existsByEmployeIdAndDate(employe.getId(), request.date())) {
            throw new BadRequestException(
                    "Une présence est déjà enregistrée pour cet employé à cette date.");
        }

        Presence presence = presenceMapper.toEntity(request, employe);
        presence.valider();

        return presenceMapper.toDto(presenceRepository.save(presence));
    }

    @Override
    @Transactional(readOnly = true)
    public PresenceDto getById(Long id) {
        return presenceMapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PresenceDto> search(PresenceFilter filter, Pageable pageable) {
        List<Long> employeIds = null;
        if (filter.employeNom() != null && !filter.employeNom().isBlank()) {
            employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(filter.employeNom());
            if (employeIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        return presenceRepository
                .findAll(PresenceSpecifications.withFilters(filter, employeIds), pageable)
                .map(presenceMapper::toDto);
    }

    private Presence findOrThrow(Long id) {
        return presenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Présence introuvable: " + id));
    }
}