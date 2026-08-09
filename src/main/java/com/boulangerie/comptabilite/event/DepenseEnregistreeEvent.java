package com.boulangerie.comptabilite.event;

import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

public record DepenseEnregistreeEvent (
        Long depenseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String motif,
        String libelle

){
}
