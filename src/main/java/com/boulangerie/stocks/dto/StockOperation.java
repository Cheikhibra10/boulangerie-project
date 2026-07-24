package com.boulangerie.stocks.dto;

import java.math.BigDecimal;

public record StockOperation(

        Long ingredientId,

        BigDecimal quantite,

        BigDecimal prixUnitaire,

        Long ligneAchatId,

        String reference,

        String motif

) {}