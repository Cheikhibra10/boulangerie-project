package com.boulangerie.abonnements.event;

import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;
import java.time.Instant;

public record VersementAbonnementEnregistreEvent(


        TypeMouvement type,

        SensMouvement sens,

        BigDecimal montant,

        TypePaiement modePaiement,

        String libelle
) {}