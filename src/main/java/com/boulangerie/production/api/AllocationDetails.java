package com.boulangerie.production.api;

import java.math.BigDecimal;

public record AllocationDetails(
        Long destinationId,
        Long produitId,
        BigDecimal quantite,
        Long livreurId,
        BigDecimal prixUnitaire
) {}