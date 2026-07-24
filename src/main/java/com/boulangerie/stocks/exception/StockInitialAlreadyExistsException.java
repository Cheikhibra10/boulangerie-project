// stocks/exception/StockInitialAlreadyExistsException.java
package com.boulangerie.stocks.exception;

import com.boulangerie.shared.exception.ConflictException;

public class StockInitialAlreadyExistsException extends ConflictException {

    public StockInitialAlreadyExistsException(Long periodeId, Long ingredientId) {
        super("Un stock initial existe déjà pour cette période et l'ingrédient "  );
    }
}