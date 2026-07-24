package com.boulangerie.stocks.dto;

import java.math.BigDecimal;

public record RetourStockLine(
        Long ingredientId,
        BigDecimal quantite,
        BigDecimal prixUnitaire,
        Long ligneAchatId,
        String motif
) {}