// stocks/exception/StockNegatifException.java
package com.boulangerie.stocks.exception;

import com.boulangerie.shared.exception.BusinessException;

public class StockNegatifException extends BusinessException {

    public StockNegatifException(Long ingredientId) {
        super("Le stock de l'ingrédient deviendrait négatif" + ingredientId );
    }

    public StockNegatifException(String message) {
        super("STOCK_NEGATIF");
    }
}