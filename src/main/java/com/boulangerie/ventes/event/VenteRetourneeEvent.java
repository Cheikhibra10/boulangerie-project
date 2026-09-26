package com.boulangerie.ventes.event;

import com.boulangerie.shared.model.Notifiable;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

// NB : non publié aujourd'hui — PaiementVenteService.rembourserRetour()/
// complementPaiement() publient déjà un MouvementCaisseEvent équivalent pour
// tout retour.
public record VenteRetourneeEvent(
        Long venteId,

        String numero,

        Long caisseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String motif,

        String libelle
) implements Notifiable {
}