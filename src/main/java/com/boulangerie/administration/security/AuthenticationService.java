package com.boulangerie.administration.security;

import com.boulangerie.administration.security.keycloak.dto.LoginRequestDto;
import com.boulangerie.administration.security.keycloak.dto.LoginResponseDto;
import com.boulangerie.shared.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RestClient securityRestClient;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public LoginResponseDto login(LoginRequestDto request) {

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();

        form.add("grant_type","password");
        form.add("client_id",clientId);
        form.add("client_secret",clientSecret);
        form.add("username",request.getEmail());
        form.add("password",request.getPassword());

        try {
            return toDto(requestToken(form));
        } catch (RestClientResponseException ex) {
            log.warn("Authentication failed: {}", ex.getResponseBodyAsString());
            throw new AuthenticationException("Email ou mot de passe incorrect", ex);
        }
    }

    public LoginResponseDto refresh(String refreshToken){

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();

        form.add("grant_type","refresh_token");
        form.add("client_id",clientId);
        form.add("client_secret",clientSecret);
        form.add("refresh_token",refreshToken);

        try {
            return toDto(requestToken(form));
        } catch (RestClientResponseException ex) {
            log.warn("Refresh failed: {}", ex.getResponseBodyAsString());
            throw new AuthenticationException("Refresh token invalide ou expiré", ex);
        }
    }

    public void logout(String refreshToken){
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();

        form.add("client_id",clientId);
        form.add("client_secret",clientSecret);
        form.add("refresh_token",refreshToken);

        try {
            securityRestClient.post()
                    .uri(serverUrl +
                            "/realms/" +
                            realm +
                            "/protocol/openid-connect/logout")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            // Déconnexion idempotente : si le refresh token est déjà expiré/révoqué côté
            // Keycloak, l'utilisateur est de toute façon déconnecté — pas la peine de faire
            // remonter une erreur pour un clic sur "se déconnecter".
            log.warn("Logout Keycloak non critique : {}", ex.getResponseBodyAsString());
        }
    }

    private LoginResponseDto toDto(AccessTokenResponse token){
        return new LoginResponseDto(
                token.getToken(),
                token.getRefreshToken(),
                token.getExpiresIn(),
                token.getRefreshExpiresIn(),
                token.getTokenType()

        );
    }
    private AccessTokenResponse requestToken(MultiValueMap<String, String> form) {

        AccessTokenResponse response = securityRestClient.post()
                .uri(serverUrl + "/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(AccessTokenResponse.class);

        if (response == null) {
            throw new AuthenticationException("Empty response returned by Keycloak");
        }
        return response;
    }

}