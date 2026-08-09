package com.boulangerie.ventes.service;

import java.math.BigDecimal;

public interface TopProduitProjection {

    Long getProduitId();

    BigDecimal getQuantiteVendue();

    BigDecimal getCaTotal();
}