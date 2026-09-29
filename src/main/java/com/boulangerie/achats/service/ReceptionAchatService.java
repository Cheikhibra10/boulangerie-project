package com.boulangerie.achats.service;

import com.boulangerie.achats.dto.ReceptionLigneDto;
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

        // Depuis la suppression du mécanisme de conversion kg/sac, la
        // quantité reçue et le prix unitaire sont déjà dans l'unité de
        // stock de l'ingrédient (plus de conversion à appliquer ici).
        List<ReceptionStockLine> stockLines = receptions.stream()
                .filter(r -> r.quantiteRecue().compareTo(BigDecimal.ZERO) > 0)

                .map(r -> {
                    LigneAchat ligne = achat.getLigne(r.ligneId());

                    return new ReceptionStockLine(
                            ligne.getIngredient().getId(),
                            r.quantiteRecue(),
                            ligne.getPrixUnitaire(),
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