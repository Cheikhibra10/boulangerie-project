package com.boulangerie.administration.model;

import java.math.BigDecimal;

public final class StockConstants {


    private StockConstants() {
    }

    /**
     * 6 sacs de farine = 300 kg.
     */
    public static final BigDecimal SEUIL_FARINE =
            BigDecimal.valueOf(6);

    /**
     * 1 sac de sucre = 25 kg.
     */
    public static final BigDecimal SEUIL_SUCRE =
            BigDecimal.ONE;

    /**
     * 1 sac de sel = 25 kg.
     */
    public static final BigDecimal SEUIL_SEL =
            BigDecimal.ONE;

    /**
     * 1 sachet de levure.
     */
    public static final BigDecimal SEUIL_LEVURE =
            BigDecimal.ONE;

    /**
     * 1 sachet d'améliorant.
     */
    public static final BigDecimal SEUIL_AMELIORANT =
            BigDecimal.ONE;

    /**
     * 10 kg de beurre.
     */
    public static final BigDecimal SEUIL_BEURRE =
            BigDecimal.TEN;

    /**
     * 10 kg de margarine.
     */
    public static final BigDecimal SEUIL_MARGARINE =
            BigDecimal.TEN;

    /**
     * 20 litres de lait.
     */
    public static final BigDecimal SEUIL_LAIT =
            BigDecimal.valueOf(20);

    /**
     * 10 kg d'œufs.
     */
    public static final BigDecimal SEUIL_OEUFS =
            BigDecimal.TEN;

    /**
     * 5 kg de chocolat.
     */
    public static final BigDecimal SEUIL_CHOCOLAT =
            BigDecimal.valueOf(5);

    /**
     * 5 kg de poudre de lait.
     */
    public static final BigDecimal SEUIL_Poudre_DE_LAIT =
            BigDecimal.valueOf(5);

    /**
     * 5 kg de cacao en poudre.
     */
    public static final BigDecimal SEUIL_CACAO =
            BigDecimal.valueOf(5);

    /**
     * 5 sachets de vanille.
     */
    public static final BigDecimal SEUIL_VANILLE =
            BigDecimal.valueOf(5);

    /**
     * 5 sachets de levure chimique.
     */
    public static final BigDecimal SEUIL_LEVURE_CHIMIQUE =
            BigDecimal.valueOf(5);

    /**
     * 5 kg de sucre glace.
     */
    public static final BigDecimal SEUIL_SUCRE_GLACE =
            BigDecimal.valueOf(5);

    /**
     * 10 litres de crème liquide.
     */
    public static final BigDecimal SEUIL_CREME_LIQUIDE =
            BigDecimal.TEN;

    /**
     * 10 kg de confiture.
     */
    public static final BigDecimal SEUIL_CONFITURE =
            BigDecimal.TEN;

    /**
     * 5 kg de noix de coco.
     */
    public static final BigDecimal SEUIL_NOIX_DE_COCO =
            BigDecimal.valueOf(5);

    /**
     * 5 kg d'arachide.
     */
    public static final BigDecimal SEUIL_ARACHIDE =
            BigDecimal.valueOf(5);

    /**
     * 5 kg de raisins secs.
     */
    public static final BigDecimal SEUIL_RAISINS_SECS =
            BigDecimal.valueOf(5);

    /**
     * Seuil des produits finis.
     */
    public static final BigDecimal SEUIL_PRODUIT =
            BigDecimal.valueOf(100);

    /**
     * Valeur par défaut pour un ingrédient non référencé.
     */
    public static final BigDecimal SEUIL_PAR_DEFAUT =
            BigDecimal.valueOf(20);

    public static final BigDecimal SEUIL_POUDRE_DE_LAIT = BigDecimal.valueOf(5);

}