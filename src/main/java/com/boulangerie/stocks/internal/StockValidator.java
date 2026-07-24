// stocks/internal/StockValidator.java
package com.boulangerie.stocks.internal;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.stocks.model.TypeMouvementStock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockValidator {

    /**
     * Valide les paramètres d'un mouvement de stock.
     */
    public void validerMouvement(Long ingredientId, TypeMouvementStock type, BigDecimal quantite, String motif) {
        if (ingredientId == null) {
            throw new BadRequestException("L'ID de l'ingrédient est obligatoire");
        }
        if (type == null) {
            throw new BadRequestException("Le type de mouvement est obligatoire");
        }
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité doit être positive");
        }
        if (type == TypeMouvementStock.PERTE && (motif == null || motif.trim().length() < 10)) {
            throw new BadRequestException("Un motif détaillé (minimum 10 caractères) est obligatoire pour une perte");
        }
    }
}