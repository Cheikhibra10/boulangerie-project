package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateContratRequest(
        LocalDate dateFin,
        BigDecimal salaireBase,
        String poste,
        StatutContrat statut
) {
}
