package com.boulangerie.production.api;

import java.math.BigDecimal;

public record BoutiqueStockDto(

        Long destinationId,

        Long produitId,

        BigDecimal quantiteInitiale,

        BigDecimal quantiteVendue,

        BigDecimal quantiteDisponible,

        BigDecimal prixUnitaire
) {}