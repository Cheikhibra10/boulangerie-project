package com.boulangerie.rh.service.impl;

import com.boulangerie.rh.dto.AvanceSalaireDto;
import com.boulangerie.rh.dto.AvanceSalaireFilter;
import com.boulangerie.rh.dto.CreateAvanceRequest;
import com.boulangerie.rh.mapper.AvanceSalaireMapper;
import com.boulangerie.rh.model.AvanceSalaire;
import com.boulangerie.rh.model.Employe;
import com.boulangerie.rh.model.StatutAvance;
import com.boulangerie.rh.repository.AvanceSalaireRepository;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.service.AvanceSalaireService;
import com.boulangerie.rh.specification.AvanceSalaireSpecifications;
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
public class AvanceSalaireServiceImpl implements AvanceSalaireService {

    private final AvanceSalaireRepository avanceSalaireRepository;
    private final EmployeRepository employeRepository;
    private final AvanceSalaireMapper avanceSalaireMapper;

    @Override
    public AvanceSalaireDto create(CreateAvanceRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable"));

        AvanceSalaire avance = avanceSalaireMapper.toEntity(request, employe);
        avance.setStatut(StatutAvance.EN_ATTENTE);
        avance.valider();

        return avanceSalaireMapper.toDto(avanceSalaireRepository.save(avance));
    }

    @Override
    @Transactional(readOnly = true)
    public AvanceSalaireDto getById(Long id) {
        return avanceSalaireMapper.toDto(findOrThrow(id));
    }

    @Override
    public AvanceSalaireDto approuver(Long id) {
        AvanceSalaire avance = findOrThrow(id);
        avance.approuver();
        return avanceSalaireMapper.toDto(avance);
    }

    @Override
    public AvanceSalaireDto refuser(Long id) {
        AvanceSalaire avance = findOrThrow(id);
        avance.refuser();
        return avanceSalaireMapper.toDto(avance);
    }

    @Override
    public AvanceSalaireDto annuler(Long id) {
        AvanceSalaire avance = findOrThrow(id);
        avance.annuler();
        return avanceSalaireMapper.toDto(avance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AvanceSalaireDto> search(AvanceSalaireFilter filter, Pageable pageable) {
        List<Long> employeIds = null;
        if (filter.employeNom() != null && !filter.employeNom().isBlank()) {
            employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(filter.employeNom());
            if (employeIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        return avanceSalaireRepository
                .findAll(AvanceSalaireSpecifications.withFilters(filter, employeIds), pageable)
                .map(avanceSalaireMapper::toDto);
    }

    private AvanceSalaire findOrThrow(Long id) {
        return avanceSalaireRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avance sur salaire introuvable: " + id));
    }
}