package com.boulangerie.livreurs.controller;

import com.boulangerie.livreurs.dto.*;
import com.boulangerie.livreurs.service.CommissionRegleService;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
@Tag(name = "Commissions", description = "Gestion des commissions ADMIN-MANAGER")
public class CommissionRegleController {

    private final CommissionRegleService service;

    @Operation(summary = "Créer commission pour un livreur")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CommissionRegleDto> create(@Valid @RequestBody CommissionRegleRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(dto));
    }

}