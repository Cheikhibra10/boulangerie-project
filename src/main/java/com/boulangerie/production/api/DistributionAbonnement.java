package com.boulangerie.production.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DistributionAbonnement(

        Long abonnementId,

        LocalDate date,

        BigDecimal quantiteDistribuee

) {}