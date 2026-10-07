// stocks/service/MouvementStockService.java
package com.boulangerie.stocks.service;

import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.stocks.dto.MouvementStockDto;
import com.boulangerie.stocks.dto.MouvementStockFilter;
import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    List<MouvementStockDto> creerMouvements(List<MouvementStock> mouvements);

    MouvementStock getMouvementEntity(Long id);

    Page<MouvementStockDto> search(MouvementStockFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}