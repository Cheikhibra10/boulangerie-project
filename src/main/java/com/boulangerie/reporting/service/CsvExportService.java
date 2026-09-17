package com.boulangerie.reporting.service;

import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;

public interface CsvExportService {

    byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    );
}