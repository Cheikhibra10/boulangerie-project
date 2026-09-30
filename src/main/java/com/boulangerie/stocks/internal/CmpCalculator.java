// stocks/internal/CmpCalculator.java
package com.boulangerie.stocks.internal;

import com.boulangerie.stocks.model.StockIngredient;
import com.boulangerie.stocks.repository.StockIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class CmpCalculator {

    private final StockIngredientRepository stockIngredientRepository;
    private static final int CMP_SCALE = 6;
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
        if (stock.getQuantite() == null || stock.getQuantite().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return stock.getValeurTotale().divide(stock.getQuantite(), CMP_SCALE, RoundingMode.HALF_UP);
    }
}