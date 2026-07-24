package com.boulangerie.administration.security.keycloak;

import com.boulangerie.administration.model.RoleUtilisateur;
import com.boulangerie.administration.security.keycloak.dto.KeycloakUserDto;
import com.boulangerie.shared.dto.UserSummary;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.service.UserDirectoryService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService implements UserDirectoryService {


    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    @Value("${keycloak.client-id}")
    private String clientId;
    /**
     * Create user in Keycloak
     */
    public String createUser(String email, String firstName, String lastName, String password){

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        CredentialRepresentation credential = new CredentialRepresentation();

        credential.setType(CredentialRepresentation.PASSWORD);

        credential.setValue(password);
        credential.setTemporary(false);

        user.setCredentials(List.of(credential));
        Response response = keycloak
                .realm(realm)
                .users()
                .create(user);

        if(response.getStatus()!=201){
            throw new RuntimeException(
                    "Erreur création utilisateur Keycloak"
            );

        }
        return CreatedResponseUtil.getCreatedId(response);
    }




    /**
     * Assign realm role
     */
    public void assignRole(String userId, RoleUtilisateur role){

        RoleRepresentation roleRepresentation = keycloak
                .realm(realm)
                .roles()
                .get(role.name())
                .toRepresentation();

        keycloak
        .realm(realm)
        .users()
        .get(userId)
        .roles()
        .realmLevel()
        .add(List.of(roleRepresentation));
    }

    /**
     * Login utilisateur Keycloak
     */

    public void logout(String refreshToken) {

        Client client = ClientBuilder.newClient();

        Form form = new Form();

        form.param("client_id", clientId);

        if (clientSecret != null && !clientSecret.isBlank()) {
            form.param("client_secret", clientSecret);
        }

        form.param("refresh_token", refreshToken);

        Response response = client
                .target(serverUrl)
                .path("/realms/" + realm + "/protocol/openid-connect/logout")
                .request()
                .post(Entity.form(form));

        if (response.getStatus() != 204) {

            throw new BadRequestException("Impossible de déconnecter l'utilisateur");

        }

        response.close();
        client.close();
    }

   public void deleteUser(String keycloakId){
        try {
            keycloak
                    .realm(realm)
                    .users()
                    .get(keycloakId)
                    .remove();
            log.info("Utilisateur Keycloak supprimé : {}", keycloakId);
        } catch (NotFoundException e){
            log.warn("Utilisateur Keycloak introuvable : {}", keycloakId
            );
        }
   }


    /**
     * Disable account
     */
    public void disableUser(String userId){
        UserResource user = keycloak
                .realm(realm)
                .users()
                .get(userId);

        UserRepresentation representation = user.toRepresentation();
        representation.setEnabled(false);
        user.update(representation);
    }





    /**
     * Enable account
     */
    public void enableUser(String userId){

        UserResource user = keycloak
                .realm(realm)
                .users()
                .get(userId);

        UserRepresentation representation = user.toRepresentation();

        representation.setEnabled(true);
        user.update(representation);
    }





    /**
     * Find users by role
     */
    public List<KeycloakUserDto> findUsersByRole(RoleUtilisateur role){

        List<UserRepresentation> users = keycloak
                .realm(realm)
                .users()
                .list();
        return users.stream()
                .filter(user ->
                        hasRole(
                           user.getId(),
                           role.name()
                        )
                )
                .map(user -> new KeycloakUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName()
                        )
                )
                .toList();
    }

    private boolean hasRole(String userId, String roleName){
        return keycloak
                .realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .anyMatch(
                        role ->
                        role.getName()
                        .equals(roleName)
                );
    }

    public RoleUtilisateur getRole(String keycloakId) {

        return keycloak.realm(realm)
                .users()
                .get(keycloakId)
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(name -> Arrays.stream(RoleUtilisateur.values())
                        .map(Enum::name)
                        .anyMatch(name::equals))
                .findFirst()
                .map(RoleUtilisateur::valueOf)
                .orElseThrow(() ->
                        new BadRequestException("Aucun rôle métier trouvé"));
    }

    public UserRepresentation getUserRepresentation(String keycloakId) {

        return keycloak
                .realm(realm)
                .users()
                .get(keycloakId)
                .toRepresentation();
    }

    @Override
    public UserSummary getUser(String keycloakId) {

        UserRepresentation user = getUserRepresentation(keycloakId);

        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}