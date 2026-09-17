package com.boulangerie.production.service;

import com.boulangerie.production.api.ProductionMensuelleReportDto;

public interface ProductionCsvExportService {

    byte[] exporterProductionMensuelle(ProductionMensuelleReportDto report);
}