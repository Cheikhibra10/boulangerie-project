// ========== CAISSE ==========
package com.boulangerie.comptabilite.dto;

import com.boulangerie.comptabilite.model.StatutCaisse;

public record CaisseFilter(
        StatutCaisse statut
) {}