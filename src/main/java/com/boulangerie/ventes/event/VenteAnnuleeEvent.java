package com.boulangerie.ventes.event;

import com.boulangerie.shared.model.Notifiable;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

// NB : non publié aujourd'hui — PaiementVenteService.rembourserAnnulation()
// publie déjà un MouvementCaisseEvent (type REMBOURSEMENT_VENTE) pour toute
// annulation.
public record VenteAnnuleeEvent(

        Long venteId,

        String numero,

        Long caisseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String motif,
        String libelle

) implements Notifiable {}