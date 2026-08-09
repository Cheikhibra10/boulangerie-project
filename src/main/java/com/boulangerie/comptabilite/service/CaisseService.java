// caisse/service/CaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.shared.dto.PageResponse;

import java.time.LocalDate;

public interface CaisseService {

    // ===== OUVRIR / FERMER =====
    CaisseDto ouvrirCaisse(OuvertureCaisseDto dto);

    CaisseDto fermerCaisse(Long caisseId, FermetureCaisseDto dto);

    CaisseDto getCaisseEnCours();

    CaisseDto getCaisse(Long id);

    PageResponse<CaisseDto> getCaisses(int page, int size);

    // ===== MOUVEMENTS =====



    // ===== JOURNAL =====
    JournalCaisseDto getJournal(Long caisseId, LocalDate dateDebut, LocalDate dateFin, String type, Long categorieId, int page, int size);


    Caisse getCaisseOuverte();


    Caisse findCaisseOrThrow(Long caisseId);

}