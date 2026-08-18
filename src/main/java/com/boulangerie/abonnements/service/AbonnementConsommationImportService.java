package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ImportConsommationCommand;

import java.util.List;

public interface AbonnementConsommationImportService {
    int importer(List<ImportConsommationCommand> commandes);
}
