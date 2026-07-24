// shared/security/CurrentUserService.java
package com.boulangerie.administration.security;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.administration.repository.UtilisateurRepository;
import com.boulangerie.administration.security.keycloak.KeycloakUserService;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UtilisateurRepository repository;
    private final KeycloakUserService keycloakUserService;

    public Utilisateur getCurrentUser(){

        String keycloakId = getCurrentKeycloakId();
        return repository.findByKeycloakId(keycloakId)
                .orElseGet(() -> synchroniserDepuisKeycloak(keycloakId));
    }

    private String getCurrentKeycloakId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return jwt.getSubject(); // same as JWT "sub"
    }

    private Utilisateur synchroniserDepuisKeycloak(String keycloakId) {
        UserRepresentation user = keycloakUserService.getUserRepresentation(keycloakId);
        if (user == null) {
            throw new BadRequestException("Utilisateur inexistant dans Keycloak");
        }
        Utilisateur utilisateur = new Utilisateur()
                .setNom(user.getLastName())
                .setPrenom(user.getFirstName())
                .setEmail(user.getEmail())
                .setRole(keycloakUserService.getRole(keycloakId))
                .setKeycloakId(keycloakId)
                .setActif(true);
        return repository.save(utilisateur);
    }
}