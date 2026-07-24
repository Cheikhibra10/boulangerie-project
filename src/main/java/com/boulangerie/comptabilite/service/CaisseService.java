// caisse/service/CaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.dto.FermetureCaisseDto;
import com.boulangerie.comptabilite.dto.JournalCaisseDto;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.dto.OuvertureCaisseDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CaisseService {

    // ===== OUVRIR / FERMER =====
    CaisseDto ouvrirCaisse(OuvertureCaisseDto dto);

    CaisseDto fermerCaisse(Long caisseId, FermetureCaisseDto dto);

    CaisseDto getCaisseEnCours();

    CaisseDto getCaisse(Long id);

    PageResponse<CaisseDto> getCaisses(int page, int size);

    // ===== MOUVEMENTS =====
    MouvementCaisseDto enregistrerPaiement(Long caisseId, BigDecimal montant, String libelle, TypePaiement modePaiement);

    MouvementCaisseDto enregistrerPaiementAbonnement(Long caisseId, BigDecimal montant, String libelle, TypePaiement modePaiement);

    MouvementCaisseDto enregistrerDepense(Long caisseId, Long categorieId, BigDecimal montant, String libelle);

    MouvementCaisseDto enregistrerVersementLivreur(Long caisseId, Long livreurId, BigDecimal montant, TypePaiement modePaiement);

    // ===== JOURNAL =====
    JournalCaisseDto getJournal(Long caisseId, LocalDate dateDebut, LocalDate dateFin, String type, Long categorieId, int page, int size);

    boolean isCaisseOuverte(Long id);

    Caisse getCaisseOuverte();

    Long getCaisseOuverteOrThrow(Long id);

    Caisse findCaisseOrThrow(Long caisseId);

}