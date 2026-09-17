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
public class ProductionLivreurDto {

    /*
     * Hidden Excel identity.
     */
    private Long livreurId;

    private String livreurNom;

    private Map<LocalDate, BigDecimal> quantites =
            new LinkedHashMap<>();

    private BigDecimal quantiteTotale =
            BigDecimal.ZERO;
}