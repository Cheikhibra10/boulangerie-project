package com.boulangerie.abonnements.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface AbonnementStatisticsApi {

    BigDecimal calculerCA(LocalDate debut, LocalDate fin);
    Integer calculerNActif();
    BigDecimal calculerCC();
}