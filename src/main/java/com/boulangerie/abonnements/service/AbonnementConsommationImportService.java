package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ImportConsommationCommand;
import com.boulangerie.abonnements.dto.ImportConsommationStats;

import java.util.List;

public interface AbonnementConsommationImportService {
    ImportConsommationStats importer(List<ImportConsommationCommand> commandes);
}
