package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;

import java.time.YearMonth;

public interface VersementsReportingService {

    VersementsRapportMensuelDto genererRapportMensuel(YearMonth periode);
}