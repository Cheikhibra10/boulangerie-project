package com.boulangerie.ventes.event;

import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

public record VenteAnnuleeEvent(

        Long venteId,

        String numero,

        Long caisseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String motif,
        String libelle

) {}