package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;

import java.time.YearMonth;

public interface AbonnementReportingService {

    ConsommationMensuelleReportDto genererRapportMensuel(
            Long abonnementId,
            YearMonth periode
    );
    ConsommationMensuelleReportDto genererRapportMensuel(
            YearMonth periode
    );

}