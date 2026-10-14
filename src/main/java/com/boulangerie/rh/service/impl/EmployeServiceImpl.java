package com.boulangerie.rh.service.impl;

import com.boulangerie.administration.api.LivreurLookupApi;
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
import com.boulangerie.shared.utils.PhoneUtils;
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
    private final LivreurLookupApi livreurLookupApi;

    @Override
    public EmployeDto create(CreateEmployeRequest request) {
        Employe employe = employeMapper.toEntity(request);
        employe.setDateEmbauche(requireDate(request.dateEmbauche(), "La date d'embauche est obligatoire"));
        employe.setStatut(StatutEmploye.ACTIF);

        // Identity: utilisateur XOR livreur XOR manual (by design)
        if (request.utilisateurId() != null) {
            linkUtilisateur(employe, request.utilisateurId());
        } else if (request.livreurId() != null) {
            linkLivreur(employe, request.livreurId());
        } else {
            applyManualIdentity(
                    employe,
                    request.prenom(),
                    request.nom(),
                    request.email(),
                    request.telephone()
            );
        }

        // Uniqueness on the telephone actually stored
        assertTelephoneUnique(employe.getTelephone(), null);

        employe = employeRepository.saveAndFlush(employe);
        employe.assignMatriculeFromId();

        return employeMapper.toDto(employeRepository.save(employe));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeDto getById(Long id) {
        return employeMapper.toDto(findOrThrow(id));
    }

    @Override
    public EmployeDto update(Long id, UpdateEmployeRequest request) {
        Employe employe = findOrThrow(id);

        if (request.utilisateurId() != null) {
            linkUtilisateur(employe, request.utilisateurId());
        }

        if (request.livreurId() != null) {
            linkLivreur(employe, request.livreurId());
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
            String tel = PhoneUtils.requiredValidFormat(
                    requireText(request.telephone(), "Le numéro de téléphone est obligatoire"));
            assertTelephoneUnique(tel, employe.getId());
            employe.setTelephone(tel);
        }

        if (request.dateSortie() != null) {
            employe.setDateSortie(request.dateSortie());
        }
        if (request.statut() != null) {
            employe.setStatut(request.statut());
        }

        return employeMapper.toDto(employe);
    }

    /**
     * Soft delete: SORTI + dateSortie.
     */
    @Override
    public void delete(Long id) {
        Employe employe = findOrThrow(id);
        if (employe.getStatut() == StatutEmploye.SORTI) {
            return;
        }
        employe.setStatut(StatutEmploye.SORTI);
        if (employe.getDateSortie() == null) {
            employe.setDateSortie(LocalDate.now());
        }
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

    // ── identity / links ─────────────────────────────────────

    private void applyManualIdentity(
            Employe employe,
            String prenom,
            String nom,
            String email,
            String telephone
    ) {
        employe.setPrenom(requireText(prenom, "Le prénom est obligatoire."));
        employe.setNom(requireText(nom, "Le nom est obligatoire."));
        employe.setEmail(blankToNull(email));
        employe.setTelephone(PhoneUtils.requiredValidFormat(
                requireText(telephone, "Le téléphone est obligatoire.")));
    }

    private void linkUtilisateur(Employe employe, Long utilisateurId) {
        employeRepository.findByUtilisateurId(utilisateurId).ifPresent(existing -> {
            if (employe.getId() == null || !existing.getId().equals(employe.getId())) {
                throw new BadRequestException(
                        "Cet utilisateur est déjà lié à un autre employé.");
            }
        });

        var user = utilisateurLookupApi.getByIdOrThrow(utilisateurId);
        employe.setUtilisateurId(user.id());
        employe.setPrenom(requireText(user.prenom(), "Le prénom de l'utilisateur est obligatoire."));
        employe.setNom(requireText(user.nom(), "Le nom de l'utilisateur est obligatoire."));
        employe.setEmail(blankToNull(user.email()));
        employe.setTelephone(PhoneUtils.requiredValidFormat(
                requireText(user.telephone(), "Le numéro de téléphone est obligatoire.")));
    }

    private void linkLivreur(Employe employe, Long livreurId) {
        employeRepository.findByLivreurId(livreurId).ifPresent(existing -> {
            if (employe.getId() == null || !existing.getId().equals(employe.getId())) {
                throw new BadRequestException(
                        "Ce livreur est déjà lié à un autre employé.");
            }
        });

        var livreur = livreurLookupApi.getByIdOrThrow(livreurId);
        employe.setLivreurId(livreur.id());
        employe.setPrenom(requireText(livreur.prenom(), "Le prénom du livreur est obligatoire."));
        employe.setNom(requireText(livreur.nom(), "Le nom du livreur est obligatoire."));
        employe.setTelephone(PhoneUtils.requiredValidFormat(
                requireText(livreur.telephone(), "Le numéro de téléphone est obligatoire.")));
    }

    /**
     * @param excludeEmployeId current employé on update (null on create)
     */
    private void assertTelephoneUnique(String telephone, Long excludeEmployeId) {
        if (telephone == null || telephone.isBlank()) {
            return;
        }
        employeRepository.findByTelephone(telephone).ifPresent(existing -> {
            if (!existing.getId().equals(excludeEmployeId)) {
                throw new BadRequestException(
                        "Un employé avec ce numéro de téléphone existe déjà.");
            }
        });
    }

    private Employe findOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable: " + id));
    }
}