// stocks/internal/StockAlerteService.java
package com.boulangerie.stocks.internal;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.stocks.event.StockSeuilAtteintEvent;
import com.boulangerie.stocks.model.StockIngredient;
import com.boulangerie.stocks.model.StockProduit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockAlerteService {

    private final ApplicationEventPublisher publisher;

    public void alerterStockBas(Ingredient ingredient, StockIngredient stock) {
        log.warn("⚠️ ALERTE : Stock bas pour l'ingrédient {} (quantité : {}, seuil : {})",
                ingredient.getLibelle(), stock.getQuantite(), stock.getSeuilAlerte());

        publisher.publishEvent(new StockSeuilAtteintEvent(
                "INGREDIENT",
                ingredient.getId(),
                ingredient.getLibelle(),
                stock.getQuantite(),
                stock.getSeuilAlerte(),
                "Stock bas : " + ingredient.getLibelle()
                        + " (" + stock.getQuantite() + " restant, seuil " + stock.getSeuilAlerte() + ")"
        ));
    }

    public void alerterStockBasProduit(Produit produit, StockProduit stock) {
        log.warn("⚠️ ALERTE : Stock bas pour le produit {} (quantité : {}, seuil : {})",
                produit.getLibelle(), stock.getQuantite(), stock.getSeuilAlerte());

        publisher.publishEvent(new StockSeuilAtteintEvent(
                "PRODUIT",
                produit.getId(),
                produit.getLibelle(),
                stock.getQuantite(),
                stock.getSeuilAlerte(),
                "Stock bas : " + produit.getLibelle()
                        + " (" + stock.getQuantite() + " restant, seuil " + stock.getSeuilAlerte() + ")"
        ));
    }

    public void alerterPerteCritique(Ingredient ingredient, StockIngredient stock, Double quantitePerdue) {
        log.warn("⚠️ ALERTE CRITIQUE : Perte importante de {} unités pour l'ingrédient {} (quantité restante : {})",
                quantitePerdue, ingredient.getLibelle(), stock.getQuantite());
    }
}