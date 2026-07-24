package com.boulangerie.stocks.dto;

import java.math.BigDecimal;

public record ReceptionStockLine(

        Long ingredientId,

        BigDecimal quantite,

        BigDecimal prixUnitaire,

        Long ligneAchatId,

        String reference

){}