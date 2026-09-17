package com.boulangerie.production.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LivreurProductionDto {

    /*
     * Identifiant technique.
     * Sera masqué dans Excel.
     */
    private Long livreurId;

    private String livreurNom;

    /*
     * GP distribué par jour.
     */
    private Map<LocalDate, BigDecimal> quantitesGp =
            new LinkedHashMap<>();

    /*
     * PP distribué par jour.
     */
    private Map<LocalDate, BigDecimal> quantitesPp =
            new LinkedHashMap<>();

    private BigDecimal totalGp = BigDecimal.ZERO;

    private BigDecimal totalPp = BigDecimal.ZERO;

    private BigDecimal totalKilo = BigDecimal.ZERO;
}