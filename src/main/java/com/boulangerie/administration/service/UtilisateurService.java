package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.RegisterUtilisateurRequestDto;
import com.boulangerie.administration.dto.UtilisateurDto;
import com.boulangerie.administration.model.RoleUtilisateur;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.service.DefaultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UtilisateurService extends DefaultService<Utilisateur, UtilisateurDto> {



    UtilisateurDto register(RegisterUtilisateurRequestDto request);
    /**
     * Authentification d'un utilisateur (pour Spring Security)
     */
    UtilisateurDto findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Liste les utilisateurs par rôle
     */
    List<UtilisateurDto> findByRole(RoleUtilisateur role);

    /**
     * Liste les utilisateurs actifs par rôle (paginé)
     */
    PageResponse<UtilisateurDto> findActiveByRole(RoleUtilisateur role, Pageable pageable);
}