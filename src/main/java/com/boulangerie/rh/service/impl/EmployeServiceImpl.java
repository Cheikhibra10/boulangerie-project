package com.boulangerie.rh.service.impl;

import com.boulangerie.administration.api.UtilisateurLookupApi;
import com.boulangerie.rh.dto.CreateEmployeRequest;
import com.boulangerie.rh.dto.EmployeDto;
import com.boulangerie.rh.dto.EmployeFilter;
import com.boulangerie.rh.dto.UpdateEmployeRequest;
import com.boulangerie.rh.mapper.EmployeMapper;
import com.boulangerie.rh.model.Employe;
import com.boulangerie.rh.model.StatutEmploye;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.service.EmployeService;
import com.boulangerie.rh.specification.EmployeSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.specification.SearchSpecifications;
import com.boulangerie.shared.utils.DateUtils;
import com.boulangerie.shared.utils.PhoneUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.boulangerie.shared.utils.DateUtils.requireDate;
import static com.boulangerie.shared.utils.ValidatorsUtils.blankToNull;
import static com.boulangerie.shared.utils.ValidatorsUtils.requireText;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeServiceImpl implements EmployeService {

    private final EmployeRepository employeRepository;
    private final EmployeMapper employeMapper;
    private final UtilisateurLookupApi utilisateurLookupApi;

    @Override
    public EmployeDto create(CreateEmployeRequest request) {
        Employe employe = employeMapper.toEntity(request);
        employe.setTelephone(PhoneUtils.isValidFormat(requireText(request.telephone(), "Le numéro de téléphone est obligatoire")));
        employe.setDateEmbauche(requireDate(request.dateEmbauche(), "La date d'embauche est obligatoire"));
        employe.setLivreurId(request.livreurId());
        employe.setStatut(StatutEmploye.ACTIF);

        if (request.utilisateurId() != null) {
            linkUtilisateur(employe, request.utilisateurId());
        } else {
            applyManualIdentity(employe, request.prenom(), request.nom(), request.email());
        }

        employe = employeRepository.saveAndFlush(employe);
        employe.assignMatriculeFromId();

        return employeMapper.toDto(employeRepository.save(employe));
    }

    private void applyManualIdentity(Employe employe, String prenom, String nom, @Email String email) {
        employe.setPrenom(requireText(prenom, "Le prénom est obligatoire."));
        employe.setNom(requireText(nom, "Le nom est obligatoire."));
        employe.setEmail(blankToNull(email));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeDto getById(Long id) {
        return employeMapper.toDto(findOrThrow(id));
    }

    @Override
    public EmployeDto update(Long id, UpdateEmployeRequest request) {
        Employe employe = findOrThrow(id);

        // Explicit link to an account
        if (request.utilisateurId() != null) {
            linkUtilisateur(employe, request.utilisateurId());
        }

        // Manual identity only when not linked to a user
        if (!employe.hasCompteUtilisateur()) {
            if (request.prenom() != null) {
                employe.setPrenom(requireText(request.prenom(), "Le prénom est obligatoire."));
            }
            if (request.nom() != null) {
                employe.setNom(requireText(request.nom(), "Le nom est obligatoire."));
            }
            if (request.email() != null) {
                employe.setEmail(blankToNull(request.email()));
            }
        }
        if (request.telephone() != null) {
            employe.setTelephone(requireText(request.telephone(), "Le numéro de téléphone est obligatoire"));
        }
        if (request.dateSortie() != null) {
            employe.setDateSortie(request.dateSortie());
        }
        if (request.statut() != null) {
            employe.setStatut(request.statut());
        }
        if (request.livreurId() != null) {
            employe.setLivreurId(request.livreurId());
        }

        // mapper for any remaining simple fields if you still want it
        // employeMapper.updateEntity(request, employe); — avoided here to keep one clear path

        return employeMapper.toDto(employe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeDto> search(EmployeFilter filter, Pageable pageable) {
        return employeRepository
                .findAll(EmployeSpecifications.withFilters(filter), pageable)
                .map(employeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        Specification<Employe> spec = Specification
                .<Employe>where(SearchSpecifications.equal("statut", StatutEmploye.ACTIF))
                .and(SearchSpecifications.likeAny(q, "nom", "prenom", "matricule"));

        return employeRepository.findAll(spec, PageRequest.of(0, 15)).stream()
                .map(e -> AutocompleteItemDto.of(
                        e.getId(),
                        e.getNomComplet(),
                        e.getMatricule()
                ))
                .toList();
    }

    private void linkUtilisateur(Employe employe, Long utilisateurId) {
        employeRepository.findByUtilisateurId(utilisateurId).ifPresent(existing -> {
            if (employe.getId() == null || !existing.getId().equals(employe.getId())) {
                throw new BadRequestException("Cet utilisateur est déjà lié à un autre employé.");
            }
        });

        var user = utilisateurLookupApi.getByIdOrThrow(utilisateurId);
        employe.setUtilisateurId(user.id());
        employe.setPrenom(requireText(user.prenom(), "Le prénom de l'utilisateur est obligatoire."));
        employe.setNom(requireText(user.nom(), "Le nom de l'utilisateur est obligatoire."));
        employe.setEmail(blankToNull(user.email()));
    }


    private Employe findOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable: " + id));
    }
}