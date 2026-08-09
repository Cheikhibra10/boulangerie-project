package com.boulangerie.achats.controller;

import com.boulangerie.achats.dto.*;
import com.boulangerie.achats.service.AchatService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achats")
@RequiredArgsConstructor
@Tag(name = "Achats", description = "Gestion des achats fournisseurs - MANAGER - ADMIN")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class AchatController {

    private final AchatService achatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AchatDto>> creerAchat(@Valid @RequestBody CreationAchatDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Achat créé", achatService.creerAchat(dto)));
    }

    @PostMapping("/{id}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LigneAchatDto>> ajouterLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneAchatRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ligne ajoutée", achatService.ajouterLigne(id, dto)));
    }

    @PutMapping("/lignes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LigneAchatDto>> modifierLigne(
            @PathVariable Long id,
            @Valid @RequestBody LigneAchatRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Ligne modifiée", achatService.modifierLigne(id, dto)));
    }

    @PostMapping("/{id}/reception")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AchatDto>> recevoirAchat(@PathVariable Long id,@Valid @RequestBody ReceptionAchatRequestDto receptions) {
        return ResponseEntity.ok(ApiResponse.success("Achat réceptionné", achatService.recevoirAchat(id,receptions)));
    }

    @PostMapping("/{id}/retour")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AchatDto>> retournerAchat(@PathVariable Long id,@Valid @RequestBody RetourAchatRequestDto retours) {
        return ResponseEntity.ok(ApiResponse.success("Achat retourné", achatService.retournerAchat(id,retours)));
    }

    @PostMapping("/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PaiementFournisseurDto>> enregistrerPaiement(
            @PathVariable Long id,
            @Valid @RequestBody PaiementFournisseurRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paiement enregistré", achatService.enregistrerPaiement(id, dto)));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<AchatDto>> annulerAchat(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Achat annulé", achatService.annulerAchat(id)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AchatDto> getAchat(@PathVariable Long id) {
        return ResponseEntity.ok(achatService.getAchat(id));
    }

    @GetMapping("/{id}/lignes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LigneAchatDto>> getLignes(@PathVariable Long id) {
        return ResponseEntity.ok(achatService.getLignesByAchat(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<AchatDto>> getAchats(
            @ModelAttribute AchatSearchRequest criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(achatService.getAchats(criteria, page, size));
    }
}