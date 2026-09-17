package com.boulangerie.production.service;

import com.boulangerie.production.api.ProductionMensuelleReportDto;

import java.time.YearMonth;

public interface ProductionReportingService {
    ProductionMensuelleReportDto genererRapportMensuel(YearMonth periode);
}
