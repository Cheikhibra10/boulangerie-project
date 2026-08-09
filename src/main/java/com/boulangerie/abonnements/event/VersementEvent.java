package com.boulangerie.abonnements.event;

import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.shared.model.TypePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

public record VersementEvent (
        TypeMouvement type,
        SensMouvement sens,
        TypePaiement modePaiement,
        BigDecimal montant,
        String libelle
){}
