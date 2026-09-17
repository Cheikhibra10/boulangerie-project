package com.boulangerie.production.service;

import com.boulangerie.production.api.ProductionMensuelleReportDto;

public interface ProductionExcelExportService {
    public byte[] exporterProductionMensuelle(ProductionMensuelleReportDto report);
}
