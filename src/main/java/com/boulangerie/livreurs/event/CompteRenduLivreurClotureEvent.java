package com.boulangerie.livreurs.event;

import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.model.TypePaiement;

import java.math.BigDecimal;

public record CompteRenduLivreurClotureEvent(
        BigDecimal montant,
        TypeMouvement type,
        SensMouvement sens,
        TypePaiement modePaiement,
        String libelle
) {
}
