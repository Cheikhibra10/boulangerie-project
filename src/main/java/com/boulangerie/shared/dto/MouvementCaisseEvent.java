package com.boulangerie.shared.dto;

import com.boulangerie.shared.model.*;

import java.math.BigDecimal;

public record MouvementCaisseEvent(

        Long caisseId,

        TypeMouvement type,

        SensMouvement sens,

        TypePaiement modePaiement,

        BigDecimal montant,

        String libelle

) implements Notifiable {
}