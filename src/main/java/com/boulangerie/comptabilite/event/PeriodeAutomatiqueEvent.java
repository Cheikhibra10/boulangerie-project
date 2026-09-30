package com.boulangerie.comptabilite.event;

import com.boulangerie.shared.model.Notifiable;

import java.math.BigDecimal;

public record PeriodeAutomatiqueEvent(
        Long periodeId,
        boolean succes,
        String libelle
) implements Notifiable {

    public BigDecimal montant() {
        return null;
    }
}