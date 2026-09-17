package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;

public interface VersementsCsvExportService {

    byte[] exporterVersementsMensuel(VersementsRapportMensuelDto rapport);
}