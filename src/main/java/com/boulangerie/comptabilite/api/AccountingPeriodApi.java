package com.boulangerie.comptabilite.api;

import java.time.LocalDate;

public interface AccountingPeriodApi {

    void verifierDateDansPeriodeOuverte(LocalDate date);

}