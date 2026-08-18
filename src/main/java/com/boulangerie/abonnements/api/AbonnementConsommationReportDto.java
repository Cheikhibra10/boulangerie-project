package com.boulangerie.abonnements.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class AbonnementConsommationReportDto {

    private Long abonnementId;

    private String abonnementNom;

    private List<ConsommationMensuelleLigneDto> lignes = new ArrayList<>();
    private BigDecimal quantiteTotale = BigDecimal.ZERO;

    private BigDecimal montantMensuelTotal = BigDecimal.ZERO;

    private BigDecimal montantVerseTotal = BigDecimal.ZERO;

    private BigDecimal reliquatTotal = BigDecimal.ZERO;
}