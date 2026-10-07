// caisse/service/MouvementCaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.comptabilite.dto.MouvementCaisseFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MouvementCaisseService {

    MouvementCaisse creerMouvement(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            String libelle,
            BigDecimal montant);

    BigDecimal calculerTotalEntrees(Long caisseId);

    BigDecimal calculerTotalSorties(Long caisseId);
//    void enregistrerPaiementAbonnement(PaiementAbonnementEnregistreEvent event);

    void creerMouvementReportBenefice(Long periodeId, BigDecimal montant);

    // Recherche par filtre + Pageable (optionnelle)
    Page<MouvementCaisseDto> search(MouvementCaisseFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);

    Page<MouvementCaisseDto> searchByCaisse(Long caisseId, MouvementCaisseFilter filter, Pageable pageable);
}