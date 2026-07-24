package com.boulangerie.administration.security.keycloak.dto;

public record KeycloakUserDto(

        String id,

        String username,

        String email,

        String firstName,

        String lastName

) {
}