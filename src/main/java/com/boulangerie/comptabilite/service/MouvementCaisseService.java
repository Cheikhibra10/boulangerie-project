// caisse/service/MouvementCaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.comptabilite.model.SensMouvement;
import com.boulangerie.comptabilite.model.TypeMouvement;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.TypePaiement;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface MouvementCaisseService {

    MouvementCaisse creerMouvementCaisse(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            Periode periode,
            String libelle,
            BigDecimal montant
    );
    MouvementCaisseDto enregistrerPaiement(
            Long caisseId,
            BigDecimal montant,
            String libelle,
            TypePaiement modePaiement
    );

    MouvementCaisseDto enregistrerPaiementAbonnement(
            Long caisseId,
            BigDecimal montant,
            String libelle,
            TypePaiement modePaiement
    );

    MouvementCaisseDto enregistrerVersementLivreur(
            Long caisseId,
            Long livreurId,
            BigDecimal montant,
            TypePaiement modePaiement
    );

    MouvementCaisseDto enregistrerDepense(
            Long caisseId,
            Long categorieId,
            BigDecimal montant,
            String libelle
    );

    PageResponse<MouvementCaisseDto> rechercher(
            Long caisseId,
            LocalDate dateDebut,
            LocalDate dateFin,
            String type,
            Long categorieId,
            Long livreurId,
            int page,
            int size
    );
    BigDecimal calculerTotalEntrees(Long caisseId);

    BigDecimal calculerTotalSorties(Long caisseId);
//    void enregistrerPaiementAbonnement(PaiementAbonnementEnregistreEvent event);



    @Transactional
    void creerMouvementReportBenefice(Long periodeId, BigDecimal montant);

//    void enregistrerVersementAbonnement(VersementAbonnementEnregistreEvent event);


    void enregistrerPaiementFournisseur(BigDecimal montant, TypePaiement typePaiement, String libelle, Instant date);
}