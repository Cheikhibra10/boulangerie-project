package com.boulangerie.stocks.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
public record StockEntry(
        Long ingredientId,
        BigDecimal quantite,
        BigDecimal prixUnitaire,
        String reference,
        Long ligneAchatId
) {}