package com.boulangerie.abonnements.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ConsommationReportProjection {

    Long getConsommationId();

    Long getLigneId();

    Long getAbonnementId();

    LocalDate getDate();

    BigDecimal getQuantite();
}