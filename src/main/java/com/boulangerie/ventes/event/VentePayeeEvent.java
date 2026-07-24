package com.boulangerie.ventes.event;

import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

public record VentePayeeEvent(

        Long venteId,

        Long caisseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String libelle

) {}