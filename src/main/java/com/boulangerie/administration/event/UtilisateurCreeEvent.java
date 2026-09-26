package com.boulangerie.administration.event;

import com.boulangerie.shared.model.Notifiable;

import java.math.BigDecimal;

public record UtilisateurCreeEvent(
        Long utilisateurId,
        String nomComplet,
        String email,
        String role,
        String libelle
) implements Notifiable {

    public BigDecimal montant() {
        return null;
    }
}