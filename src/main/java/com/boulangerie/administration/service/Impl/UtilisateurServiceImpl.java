// administration/service/impl/UtilisateurServiceImpl.java
package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.RegisterUtilisateurRequestDto;
import com.boulangerie.administration.dto.UtilisateurDto;
import com.boulangerie.administration.mapper.UtilisateurMapper;
import com.boulangerie.administration.model.RoleUtilisateur;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.administration.repository.UtilisateurRepository;
import com.boulangerie.administration.service.UtilisateurService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.administration.security.keycloak.KeycloakUserService;
import com.boulangerie.administration.security.keycloak.dto.KeycloakUserDto;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import com.boulangerie.shared.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class UtilisateurServiceImpl extends AbstractCrudService<Utilisateur, UtilisateurDto> implements UtilisateurService {
    private final UtilisateurRepository repository;
    private final KeycloakUserService keycloakUserService;

    public UtilisateurServiceImpl(
            UtilisateurRepository repository,
            UtilisateurMapper mapper,
            KeycloakUserService keycloakUserService
    ) {

        super(repository, mapper, Utilisateur.class);

        this.repository = repository;
        this.keycloakUserService = keycloakUserService;
    }



    /**
     * Create a new application user.
     *
     * Workflow:
     * 1 - Validate email
     * 2 - Create user in Keycloak
     * 3 - Assign Keycloak role
     * 4 - Save business profile locally
     */
    @Override
    public UtilisateurDto register(RegisterUtilisateurRequestDto request){

        if(repository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email déjà utilisé");
        }

        String keycloakId = null;
        try {
            // 1 - Create Keycloak user
             keycloakId = keycloakUserService.createUser(
                    request.getEmail(),
                    request.getPrenom(),
                    request.getNom(),
                    request.getPassword()
            );

            log.info("Keycloak user created id={}", keycloakId);

            log.info("Assign role {}", request.getRole());
            // 2 - Assign role Keycloak
            keycloakUserService.assignRole(keycloakId, request.getRole());
            log.info("Role assigned");
            // 3 - Save application user
            Utilisateur utilisateur = new Utilisateur()
                    .setNom(request.getNom())
                    .setPrenom(request.getPrenom())
                    .setEmail(request.getEmail())
                    .setRole(request.getRole())
                    .setKeycloakId(keycloakId)
                    .setActif(true);

            return mapper.toDto(repository.save(utilisateur));
        } catch (Exception e) {
            if(keycloakId != null){
                keycloakUserService.deleteUser(keycloakId);
            }
            log.error("Erreur pendant création utilisateur", e);
            throw new BadRequestException(
                    "Création utilisateur impossible : " + e.getMessage()
            );
        }
    }


    /**
     * Update user profile.
     *
     * Password and roles are managed by Keycloak.
     */
    @Override
    public UtilisateurDto update(Long id, UtilisateurDto dto) {
        Utilisateur existing = getEntityById(id);

        repository.findByEmail(dto.getEmail())
                .ifPresent(user -> {
                    if (!user.getId().equals(id)) {
                        throw new BadRequestException("Un utilisateur avec cet email existe déjà.");
                    }
                });

        existing
                .setNom(dto.getNom())
                .setPrenom(dto.getPrenom())
                .setTelephone(dto.getTelephone())
                .setEmail(dto.getEmail());

        Utilisateur updated = repository.save(existing);
        return mapper.toDto(updated);
    }

    /**
     * Partial update.
     *
     * Security fields are ignored.
     */
    @Override
    public UtilisateurDto patchFields(Long id, UtilisateurDto dto) {
        dto.setPassword(null);
        dto.setRole(null);
        return super.patchFields(id, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto findByEmail(String email) {

        return repository.findByEmail(email)
                .map(mapper::toDto)
                .orElseThrow(() ->
                        new BadRequestException("Aucun utilisateur trouvé avec l'email : " + email)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    /**
     * Find users by Keycloak role.
     *
     * Note:
     * This should ideally query Keycloak.
     * Database does not own roles anymore.
     */
    @Override
    public List<UtilisateurDto> findByRole(RoleUtilisateur role) {
        return keycloakUserService
                .findUsersByRole(role)
                .stream()
                .map(this::mapKeycloakUserToDto)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UtilisateurDto> findActiveByRole(RoleUtilisateur role, Pageable pageable) {
        return PageUtils.toPageResponse(
                repository.findByActifTrue(pageable)
                        .map(mapper::toDto)
        );
    }

    /**
     * Archive local profile.
     *
     * User cannot access business features.
     */
    @Override
    public UtilisateurDto archive(Long id) {

        Utilisateur utilisateur = getEntityById(id);
        utilisateur.setActif(false);
        /*
         * Disable Keycloak account
         */
        keycloakUserService.disableUser(utilisateur.getKeycloakId());
        return mapper.toDto(repository.save(utilisateur));
    }

    /**
     * Restore user.
     */
    @Override
    public UtilisateurDto restore(Long id) {
        Utilisateur utilisateur = getEntityById(id);
        utilisateur.setActif(true);
        keycloakUserService.enableUser(utilisateur.getKeycloakId());
       return mapper.toDto(repository.save(utilisateur));
    }

    private UtilisateurDto mapKeycloakUserToDto(KeycloakUserDto keycloakUser) {
        UtilisateurDto dto = new UtilisateurDto();
        dto.setEmail(keycloakUser.email());
        dto.setNom(keycloakUser.lastName());
        dto.setPrenom(keycloakUser.firstName());
        return dto;
    }

}