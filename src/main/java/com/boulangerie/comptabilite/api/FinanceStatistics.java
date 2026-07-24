package com.boulangerie.comptabilite.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface FinanceStatistics {
    BigDecimal calculerTotalCharges(LocalDate debut, LocalDate fin);

}