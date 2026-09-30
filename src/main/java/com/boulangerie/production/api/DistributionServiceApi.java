package com.boulangerie.production.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DistributionServiceApi {
    BigDecimal getQuantiteDistribuee(
            Long abonnementId,
            LocalDate date
    );
}