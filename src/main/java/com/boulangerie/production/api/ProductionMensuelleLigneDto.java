package com.boulangerie.production.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProductionMensuelleLigneDto {

    /*
     * Hidden identity used by Excel.
     */
    private Long produitId;

    private String produitLibelle;

    private String typeProduit;

    /*
     * Quantités GP par jour.
     */
    private Map<LocalDate, BigDecimal> quantitesGp =
            new LinkedHashMap<>();

    /*
     * Quantités PP par jour.
     */
    private Map<LocalDate, BigDecimal> quantitesPp =
            new LinkedHashMap<>();

    /*
     * Total mensuel GP.
     */
    private BigDecimal quantiteGpTotale =
            BigDecimal.ZERO;

    /*
     * Total mensuel PP.
     */
    private BigDecimal quantitePpTotale =
            BigDecimal.ZERO;

    /*
     * Production prévue.
     */
    private BigDecimal quantitePrevue =
            BigDecimal.ZERO;

    /*
     * Sacs de farine utilisés.
     */
    private BigDecimal sacsFarineUtilises =
            BigDecimal.ZERO;
}