// administration/controller/UtilisateurController.java
package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.RegisterUtilisateurRequestDto;
import com.boulangerie.administration.dto.UtilisateurDto;
import com.boulangerie.administration.model.RoleUtilisateur;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.administration.service.UtilisateurService;
import com.boulangerie.shared.controller.GenericCrudController;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/utilisateurs")
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs du système")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class UtilisateurController
        extends GenericCrudController<Utilisateur, UtilisateurDto> {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService service) {
        super(service);
        this.utilisateurService = service;
    }


    // ===================== MÉTHODES SPÉCIFIQUES =====================

    @Operation(summary = "Rechercher un utilisateur par email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurDto> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok(utilisateurService.findByEmail(email));
    }

    @Operation(summary = "Lister les utilisateurs par rôle")
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDto>> findByRole(@PathVariable RoleUtilisateur role) {
        return ResponseEntity.ok(utilisateurService.findByRole(role));
    }

    @Operation(summary = "Lister les utilisateurs actifs par rôle (paginé)")
    @GetMapping("/role/{role}/actifs")
    public ResponseEntity<PageResponse<UtilisateurDto>> findActiveByRole(
            @PathVariable RoleUtilisateur role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(utilisateurService.findActiveByRole(role, PageRequest.of(page, size)));
    }

    @Operation(summary = "Vérifier si un email existe déjà")
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(utilisateurService.existsByEmail(email));
    }
}