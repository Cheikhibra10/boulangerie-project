// caisse/service/CaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.*;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface CaisseService {

    // ===== OUVRIR / FERMER =====
    CaisseDto ouvrirCaisse(OuvertureCaisseDto dto);

    CaisseDto fermerCaisse(Long caisseId, FermetureCaisseDto dto);

    CaisseDto getCaisseEnCours();

    CaisseDto getCaisse(Long id);

    PageResponse<CaisseDto> getCaisses(int page, int size);

    // ===== MOUVEMENTS =====



    // ===== JOURNAL =====


    JournalCaisseDto getJournal(
            Long caisseId,
            LocalDate dateDebut,
            LocalDate dateFin,
            TypeMouvement type,
            SensMouvement sens,
            String libelle,
            int page,
            int size
    );

    Caisse getCaisseOuverte();


    Caisse findCaisseOrThrow(Long caisseId);

    Page<CaisseDto> search(CaisseFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}