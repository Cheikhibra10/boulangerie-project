package com.boulangerie.abonnements.event;

import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;
import java.time.Instant;

public record PaiementAbonnementEnregistreEvent(
        Long paiementId,
        Long abonnementId,
        Long ligneId,
        BigDecimal montant,
        TypePaiement modePaiement,
        String libelle,
        Instant date
) {}