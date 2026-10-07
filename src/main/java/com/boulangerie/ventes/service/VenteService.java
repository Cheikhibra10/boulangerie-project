// ventes/service/VenteService.java
package com.boulangerie.ventes.service;

import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.ventes.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface VenteService {

    // ===== CRÉATION =====
    VenteDto creerVente(VenteRequestDto dto);


    // ===== INVENTAIRE RESTANTS =====
    InventaireResultDto enregistrerRestants(Long venteId, InventaireDto dto);
    VenteDto annulerVente(Long venteId, AnnulationVenteRequestDto dto);
    // ===== CONSULTATION =====
    VenteDto getVente(Long id);
    VenteDto retournerVente(Long venteId, RetourVenteRequestDto dto);
    PageResponse<VenteDto> getVentes(LocalDate date, Long produitId, int page, int size);

    Page<VenteDto> search(VenteBoutiqueFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}