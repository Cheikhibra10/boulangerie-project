// stocks/service/MouvementStockService.java
package com.boulangerie.stocks.service;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.stocks.dto.MouvementStockDto;
import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MouvementStockService {

    MouvementStockDto creerMouvement(
            TypeMouvementStock type,
            Long ingredientId,
            BigDecimal quantite,
            BigDecimal montant,
            String motif,
            Long lotProductionId,
            Long ligneAchatId
    );

    PageResponse<MouvementStockDto> rechercher(
            Long ingredientId,
            LocalDate dateDebut,
            LocalDate dateFin,
            String type,
            String statut,
            int page,
            int size
    );
    List<MouvementStockDto> creerMouvements(List<MouvementStock> mouvements);

    MouvementStock getMouvementEntity(Long id);
}