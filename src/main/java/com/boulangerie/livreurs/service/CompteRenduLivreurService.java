// livreurs/service/LivreurService.java
package com.boulangerie.livreurs.service;

import com.boulangerie.livreurs.dto.*;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CompteRenduLivreurService {

    // ===== COMPTE RENDU =====
    CompteRenduDto creerOuRecupererCompteRendu(CompteRenduCreationDto dto);


    CompteRenduDto cloturerCompteRendu(Long journalierId, ClotureCompteRenduDto dto);

    // ===== CONSULTATION =====
    CompteRenduDto getCompteRendu(Long id);

    PageResponse<CompteRenduDto> getHistorique(Long livreurId, LocalDate dateDebut, LocalDate dateFin, int page, int size);

    Page<CompteRenduDto> search(CompteLivreurJournalierFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}