package com.boulangerie.administration.model;

import java.math.BigDecimal;

public final class StockConstants {

    private StockConstants() {
    }

    /**
     * 6 sacs de farine.
     */
    public static final BigDecimal SEUIL_FARINE =
            BigDecimal.valueOf(300);

    /**
     * 1 sac de sucre.
     */
    public static final BigDecimal SEUIL_SUCRE =
            BigDecimal.valueOf(150);

    /**
     * 1 carton de levure.
     */
    public static final BigDecimal SEUIL_LEVURE =
            BigDecimal.valueOf(250);

    public static final BigDecimal SEUIL_AMELIORANT =
            BigDecimal.valueOf(250);

    /**
     * Produits finis.
     */
    public static final BigDecimal SEUIL_PRODUIT =
            BigDecimal.valueOf(100);

    /**
     * Valeur par défaut.
     */
    public static final BigDecimal SEUIL_PAR_DEFAUT =
            BigDecimal.valueOf(20);

}