// stocks/exception/StockInsuffisantException.java
package com.boulangerie.stocks.exception;

import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.ConflictException;

public class StockInsuffisantException extends ConflictException {

    public StockInsuffisantException(Long ingredientId) {
        super("Stock insuffisant pour l'ingrédient " + ingredientId);
    }

    public StockInsuffisantException(String message) {
        super(message);
    }
}