// stocks/internal/CmpCalculator.java
package com.boulangerie.stocks.internal;

import com.boulangerie.stocks.model.StockIngredient;
import com.boulangerie.stocks.repository.StockIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CmpCalculator {

    private final StockIngredientRepository stockIngredientRepository;

    /**
     * Calcule le coût moyen pondéré (CMP) pour un ingrédient donné.
     */
    public BigDecimal calculerCMP(Long ingredientId) {
        return stockIngredientRepository.findByIngredientId(ingredientId)
                .map(this::calculerCMP)
                .orElse(BigDecimal.ZERO);
    }
    /**
     * Calcule le CMP à partir d'un stock.
     */
    public BigDecimal calculerCMP(StockIngredient stock) {
        if (stock.getQuantite().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return stock.getValeurTotale().divide(stock.getQuantite(), 2, BigDecimal.ROUND_HALF_UP);
    }
}