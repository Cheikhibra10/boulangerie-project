package com.boulangerie.achats.service;

import com.boulangerie.achats.dto.RetourLigneDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.dto.ValeursStock;
import com.boulangerie.stocks.dto.RetourStockLine;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.model.LigneAchat;
import com.boulangerie.stocks.api.StockManagement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetourAchatService {

    private final StockManagement stockManagement;

    @Transactional
    public void retourner(Achat achat, List<RetourLigneDto> retours) {

        List<RetourStockLine> stockLines = retours.stream()

                        .filter(r -> r.quantiteRetournee().compareTo(BigDecimal.ZERO) > 0)
                        .map(r -> {
                            LigneAchat ligne = achat.getLigne(r.ligneId());
                            Ingredient ingredient = ligne.getIngredient();
                            ValeursStock valeurs = ingredient.convertirVersStock(r.quantiteRetournee(), ligne.getPrixUnitaire());
                            return new RetourStockLine(

                                    ligne.getIngredient().getId(),

                                    valeurs.quantite(),

                                    valeurs.prixUnitaire(),

                                    ligne.getId(),

                                    r.motif()

                            );
                        }).toList();

        if (!stockLines.isEmpty()) {
            stockManagement.retournerAchat(
                    achat.getId(),
                    stockLines
            );
        }
    }
}