package com.boulangerie.ventes.event;

import com.boulangerie.shared.model.Notifiable;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

// NB : non publié aujourd'hui — VenteServiceImpl.creerVente() publie déjà un
// MouvementCaisseEvent équivalent pour ce même paiement.
public record VentePayeeEvent(

        Long venteId,

        Long caisseId,

        BigDecimal montant,

        TypePaiement modePaiement,

        String libelle

) implements Notifiable {}