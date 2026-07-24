package com.boulangerie.administration.security.keycloak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    private String refreshToken;

}