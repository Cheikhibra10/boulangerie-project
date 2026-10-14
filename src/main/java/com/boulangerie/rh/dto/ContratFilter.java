package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.*;

public record ContratFilter(
        String employeNom,
        TypeContrat type,
        StatutContrat statut
) {
}
