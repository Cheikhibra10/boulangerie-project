package com.boulangerie.reporting.service;

import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;

public interface ExcelExportService {

    byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    );
}