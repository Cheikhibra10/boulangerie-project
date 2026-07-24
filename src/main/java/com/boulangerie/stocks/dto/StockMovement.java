package com.boulangerie.stocks.dto;

import java.math.BigDecimal;

public record StockMovement(
        Long produitId,
        BigDecimal quantite
){}