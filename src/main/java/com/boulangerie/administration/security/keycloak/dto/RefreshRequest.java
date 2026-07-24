package com.boulangerie.administration.security.keycloak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {

    private String refreshToken;

}