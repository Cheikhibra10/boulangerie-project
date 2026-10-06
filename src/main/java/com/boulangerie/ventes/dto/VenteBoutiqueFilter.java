// ========== VENTE BOUTIQUE ==========
package com.boulangerie.ventes.dto;

import com.boulangerie.ventes.model.StatutVente;

public record VenteBoutiqueFilter(
        StatutVente statut
) {}