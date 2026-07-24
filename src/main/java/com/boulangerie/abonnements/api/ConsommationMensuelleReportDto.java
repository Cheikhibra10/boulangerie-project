package com.boulangerie.abonnements.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ConsommationMensuelleReportDto {

    private Long abonnementId;

    private String abonnement;

    private YearMonth periode;

    private List<ConsommationMensuelleLigneDto> lignes =
            new ArrayList<>();

    private BigDecimal quantiteTotale = BigDecimal.ZERO;

    private BigDecimal montantTotal = BigDecimal.ZERO;

    private BigDecimal montantPaye = BigDecimal.ZERO;

    private BigDecimal reliquatTotal = BigDecimal.ZERO;

}