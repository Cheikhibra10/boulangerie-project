package com.boulangerie.achats.event;

import com.boulangerie.shared.model.Notifiable;

import java.math.BigDecimal;

public record AchatReceptionneEvent(
        Long achatId,
        String fournisseurNom,
        BigDecimal montantTotal,
        String libelle
) implements Notifiable {

    public BigDecimal montant() {
        return montantTotal;
    }
}