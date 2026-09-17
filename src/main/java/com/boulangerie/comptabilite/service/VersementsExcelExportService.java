package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;

public interface VersementsExcelExportService {

    byte[] exporterVersementsMensuel(VersementsRapportMensuelDto rapport);
}