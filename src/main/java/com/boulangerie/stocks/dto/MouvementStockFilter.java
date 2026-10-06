// ========== MOUVEMENT STOCK ==========
package com.boulangerie.stocks.dto;

import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;

public record MouvementStockFilter(
        String ingredientNom,
        TypeMouvementStock type,
        StatutMouvement statut
) {}