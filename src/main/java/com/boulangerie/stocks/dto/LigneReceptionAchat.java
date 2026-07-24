package com.boulangerie.stocks.dto;

import java.math.BigDecimal;

public record LigneReceptionAchat(
        Long ingredientId,
        BigDecimal quantite,
        BigDecimal prixUnitaire,
        Long ligneAchatId
) {}