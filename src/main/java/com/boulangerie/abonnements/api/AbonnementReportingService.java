package com.boulangerie.abonnements.api;

import java.time.YearMonth;

public interface AbonnementReportingService {

    ConsommationMensuelleReportDto  genererRapportMensuel(
            Long abonnementId,
            YearMonth periode
    );

}