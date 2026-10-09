package com.boulangerie.administration.controller;

import com.boulangerie.administration.security.AuthenticationService;
import com.boulangerie.administration.security.keycloak.dto.LoginRequestDto;
import com.boulangerie.administration.security.keycloak.dto.LoginResponseDto;
import com.boulangerie.administration.dto.RegisterUtilisateurRequestDto;
import com.boulangerie.administration.dto.UtilisateurDto;
//import com.boulangerie.administration.security.keycloak.KeycloakUserService;
import com.boulangerie.administration.security.keycloak.dto.LogoutRequest;
import com.boulangerie.administration.security.keycloak.dto.RefreshRequest;
import com.boulangerie.administration.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UtilisateurService utilisateurService;

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public LoginResponseDto login(
            @RequestBody @Valid LoginRequestDto request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = resolveClientIp(httpRequest);
        return authenticationService.login(request, clientIp);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // First IP in the chain is the original client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/refresh")
    public LoginResponseDto refresh(@RequestBody RefreshRequest request){
        return authenticationService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request){
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Operation(summary="Register un utilisateur")
    public ResponseEntity<UtilisateurDto> register(@Valid @RequestBody RegisterUtilisateurRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.register(request));

    }
}