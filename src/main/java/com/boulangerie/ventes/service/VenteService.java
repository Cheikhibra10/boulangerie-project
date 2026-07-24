// ventes/service/VenteService.java
package com.boulangerie.ventes.service;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.ventes.dto.*;

import java.time.LocalDate;

public interface VenteService {

    // ===== CRÉATION =====
    VenteDto creerVente(VenteRequestDto dto);


    // ===== INVENTAIRE RESTANTS =====
    InventaireResultDto enregistrerRestants(Long venteId, InventaireDto dto);

    // ===== CONSULTATION =====
    VenteDto getVente(Long id);
    PageResponse<VenteDto> getVentes(LocalDate date, Long produitId, int page, int size);
}