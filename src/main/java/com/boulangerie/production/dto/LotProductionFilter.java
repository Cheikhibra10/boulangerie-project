// ========== LOT PRODUCTION ==========
package com.boulangerie.production.dto;

import com.boulangerie.production.model.StatutProduction;

import java.time.LocalDate;

public record LotProductionFilter(
        StatutProduction statut,
        String produitLibelle,
        LocalDate dateDebut,
        LocalDate dateFin
) {}