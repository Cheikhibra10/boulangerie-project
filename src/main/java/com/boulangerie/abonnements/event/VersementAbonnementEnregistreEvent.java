package com.boulangerie.abonnements.event;

import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;
import java.time.Instant;

public record VersementAbonnementEnregistreEvent(

        Long versementId,

        Long abonnementId,

        Long compteId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String libelle,

        Instant date

) {}