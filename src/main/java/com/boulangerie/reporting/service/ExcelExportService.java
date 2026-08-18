package com.boulangerie.reporting.service;

import com.boulangerie.abonnements.api.ConsommationMensuelleReportDto;

public interface ExcelExportService {

    byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    );
}