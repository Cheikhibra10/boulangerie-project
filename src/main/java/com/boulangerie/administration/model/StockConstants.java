package com.boulangerie.administration.model;

import java.math.BigDecimal;

public final class StockConstants {

    private StockConstants() {
    }

    /**
     * 6 sacs de farine.
     */
    public static final BigDecimal SEUIL_FARINE =
            BigDecimal.valueOf(6);

    /**
     * 1 sac de sucre.
     */
    public static final BigDecimal SEUIL_SUCRE =
            BigDecimal.ONE;

    /**
     * 1 carton de levure.
     */
    public static final BigDecimal SEUIL_LEVURE =
            BigDecimal.ONE;

    public static final BigDecimal SEUIL_AMELIORANT =
                BigDecimal.ONE;

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