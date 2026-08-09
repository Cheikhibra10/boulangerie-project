// caisse/service/MouvementCaisseService.java
package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.model.*;
import com.boulangerie.shared.model.TypePaiement;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MouvementCaisseService {

    MouvementCaisse creerMouvement(
            TypeMouvement type,
            SensMouvement sens,
            TypePaiement modePaiement,
            Caisse caisse,
            String libelle,
            BigDecimal montant);

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

    void creerMouvementReportBenefice(Long periodeId, BigDecimal montant);

}