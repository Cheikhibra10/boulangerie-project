package com.boulangerie.rh.service.impl;

import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.mapper.ContratMapper;
import com.boulangerie.rh.model.*;
import com.boulangerie.rh.repository.ContratRepository;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.service.ContratService;
import com.boulangerie.rh.specification.ContratSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.specification.SearchSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratServiceImpl implements ContratService {

    private final ContratRepository contratRepository;
    private final EmployeRepository employeRepository;
    private final ContratMapper contratMapper;

    @Override
    public ContratDto create(CreateContratRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable"));

        if (contratRepository.existsByEmployeIdAndStatut(employe.getId(), StatutContrat.ACTIF)) {
            throw new BadRequestException("Cet employé a déjà un contrat actif.");
        }

        Contrat contrat = contratMapper.toEntity(request, employe);
        contrat.setStatut(StatutContrat.ACTIF);
        contrat.valider();

        return contratMapper.toDto(contratRepository.save(contrat));
    }

    @Override
    @Transactional(readOnly = true)
    public ContratDto getById(Long id) {
        return contratMapper.toDto(findOrThrow(id));
    }

    @Override
    public ContratDto update(Long id, UpdateContratRequest request) {
        Contrat contrat = findOrThrow(id);
        contratMapper.updateEntity(request, contrat);
        if (request.salaireBase() != null || request.dateFin() != null) {
            contrat.valider();
        }
        return contratMapper.toDto(contrat);
    }

    @Override
    public ContratDto resilier(Long id, LocalDate dateFinEffective) {
        Contrat contrat = findOrThrow(id);
        contrat.resilier(dateFinEffective);
        return contratMapper.toDto(contrat);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratDto> search(ContratFilter filter, Pageable pageable) {
        List<Long> employeIds = null;
        if (filter.employeNom() != null && !filter.employeNom().isBlank()) {
            employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(filter.employeNom());
            if (employeIds.isEmpty()) return Page.empty(pageable);
        }
        return contratRepository
                .findAll(ContratSpecifications.withFilters(filter, employeIds), pageable)
                .map(contratMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) return List.of();

        List<Long> employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(q);
        if (employeIds.isEmpty()) return List.of();

        Specification<Contrat> spec = Specification
                .<Contrat>where(ContratSpecifications.employeIdsIn(employeIds))
                .and(SearchSpecifications.equal("statut", StatutContrat.ACTIF));

        return contratRepository.findAll(spec, PageRequest.of(0, 15)).stream()
                .map(c -> AutocompleteItemDto.of(
                        c.getId(),
                        c.getEmploye().getNomComplet() + " — " + c.getType(),
                        c.getPoste()
                ))
                .toList();
    }

    private Contrat findOrThrow(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contrat introuvable: " + id));
    }
}