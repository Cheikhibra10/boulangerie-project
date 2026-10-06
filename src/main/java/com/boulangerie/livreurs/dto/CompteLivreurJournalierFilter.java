// ========== COMPTE RENDU LIVREUR ==========
package com.boulangerie.livreurs.dto;

import com.boulangerie.livreurs.model.StatutCompteRendu;

public record CompteLivreurJournalierFilter(
        StatutCompteRendu statut,
        String livreurNom
) {}