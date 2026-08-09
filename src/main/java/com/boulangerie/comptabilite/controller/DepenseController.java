package com.boulangerie.comptabilite.controller;

import com.boulangerie.comptabilite.dto.*;
import com.boulangerie.comptabilite.service.DepenseService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depenses")
@RequiredArgsConstructor
@Tag(name = "Gestion des Dépenses ADMIN-MANAGER")
public class DepenseController {

    private final DepenseService depenseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<DepenseDto>> enregistrer(
            @Valid @RequestBody EnregistrerDepenseDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dépense enregistrée",
                        depenseService.enregistrerDepense(dto)
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DepenseDto> get(@PathVariable Long id) {

        return ResponseEntity.ok(depenseService.getDepense(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PageResponse<DepenseDto>> getAll(
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) Long periodeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(depenseService.getDepenses(categorieId, periodeId, page, size));
    }
}