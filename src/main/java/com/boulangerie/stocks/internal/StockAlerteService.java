// stocks/internal/StockAlerteService.java
package com.boulangerie.stocks.internal;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.stocks.model.StockIngredient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StockAlerteService {

    /**
     * Alerte lorsqu'un stock passe sous le seuil d'alerte.
     */
    public void alerterStockBas(Ingredient ingredient, StockIngredient stock) {
        log.warn("⚠️ ALERTE : Stock bas pour l'ingrédient {} (quantité : {}, seuil : {})",
                ingredient.getLibelle(), stock.getQuantite(), stock.getSeuilAlerte());
        // TODO: Implémenter l'envoi d'email ou notification in-app
    }

    /**
     * Alerte pour une perte critique.
     */
    public void alerterPerteCritique(Ingredient ingredient, StockIngredient stock, Double quantitePerdue) {
        log.warn("⚠️ ALERTE CRITIQUE : Perte importante de {} unités pour l'ingrédient {} (quantité restante : {})",
                quantitePerdue, ingredient.getLibelle(), stock.getQuantite());
        // TODO: Notifier le Manager
    }
}