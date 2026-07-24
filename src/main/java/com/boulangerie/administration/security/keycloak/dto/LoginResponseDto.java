package com.boulangerie.administration.security.keycloak.dto;

import lombok.*;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private Long refreshExpiresIn;

    private String tokenType;

}