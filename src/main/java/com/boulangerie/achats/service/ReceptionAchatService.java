package com.boulangerie.achats.service;

import com.boulangerie.achats.dto.ReceptionLigneDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.dto.ValeursStock;
import com.boulangerie.stocks.dto.ReceptionStockLine;
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
@Transactional
public class ReceptionAchatService {

    private final StockManagement stockManagement;

    public void recevoirAchat(Achat achat, List<ReceptionLigneDto> receptions) {

        List<ReceptionStockLine> stockLines = receptions.stream()
                .filter(r -> r.quantiteRecue().compareTo(BigDecimal.ZERO) > 0)

                .map(r -> {
                    LigneAchat ligne = achat.getLigne(r.ligneId());
                    Ingredient ingredient = ligne.getIngredient();
                    ValeursStock valeurs = ingredient.convertirVersStock(r.quantiteRecue(), ligne.getPrixUnitaire());

                    return new ReceptionStockLine(
                            ligne.getIngredient().getId(),
                            valeurs.quantite(),
                            valeurs.prixUnitaire(),
                            ligne.getId(),
                            null
                    );
                })
                .toList();

        if (!stockLines.isEmpty()) {
            stockManagement.receptionnerAchat(
                    achat.getId(),
                    stockLines
            );
        }
    }
}