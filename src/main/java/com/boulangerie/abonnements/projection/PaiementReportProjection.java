package com.boulangerie.abonnements.projection;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaiementReportProjection {

    Long getPaiementId();

    Long getLigneId();

    Long getAbonnementId();

    BigDecimal getMontant();

    Instant getCreatedAt();
}