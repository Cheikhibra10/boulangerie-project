// ========== LOT PRODUCTION ==========
package com.boulangerie.production.dto;

import com.boulangerie.production.model.StatutProduction;

public record LotProductionFilter(
        String produitNom,
        StatutProduction statut
) {}