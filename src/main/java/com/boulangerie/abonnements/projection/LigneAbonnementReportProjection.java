package com.boulangerie.abonnements.projection;

import java.math.BigDecimal;

public interface LigneAbonnementReportProjection {

    Long getLigneId();

    Long getAbonnementId();

    Long getClientId();

    String getClientNom();

    String getClientPrenom();

    BigDecimal getPrixUnitaire();

    BigDecimal getReliquat();
}