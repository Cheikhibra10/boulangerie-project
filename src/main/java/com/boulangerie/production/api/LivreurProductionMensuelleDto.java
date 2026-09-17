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
public class LivreurProductionMensuelleDto {
    /*
     * Hidden Excel identity.
     */
    private Long livreurId;

    private String nom;

    private Map<LocalDate, BigDecimal> quantitesGp = new LinkedHashMap<>();

    private Map<LocalDate, BigDecimal> quantitesPp = new LinkedHashMap<>();

    private BigDecimal quantiteGpTotale = BigDecimal.ZERO;

    private BigDecimal quantitePpTotale = BigDecimal.ZERO;
}