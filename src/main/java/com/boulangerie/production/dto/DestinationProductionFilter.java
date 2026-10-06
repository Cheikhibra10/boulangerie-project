// ========== DESTINATION PRODUCTION ==========
package com.boulangerie.production.dto;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.EtatPain;

public record DestinationProductionFilter(
        CanalDistribution canal,
        EtatPain etatPain,
        String livreurNom
) {}