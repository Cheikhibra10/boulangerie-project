// ========== ACHATS ==========
package com.boulangerie.achats.dto;

import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.StatutPaiement;
import com.boulangerie.achats.model.StatutReception;

public record AchatFilter(
        String fournisseurNom,
        StatutReception statutReception,
        StatutPaiement statutPaiement,
        StatutAchat statutAchat
) {}