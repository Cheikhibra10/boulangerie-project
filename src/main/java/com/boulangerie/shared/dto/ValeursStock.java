package com.boulangerie.shared.dto;

import java.math.BigDecimal;

public record ValeursStock(
        BigDecimal quantite,
        BigDecimal prixUnitaire
) {}