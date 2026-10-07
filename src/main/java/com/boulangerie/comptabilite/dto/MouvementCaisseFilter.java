package com.boulangerie.comptabilite.dto;

import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.shared.model.*;


import java.time.LocalDate;

public record MouvementCaisseFilter(
        TypeMouvement typeMouvement,
        SensMouvement sens,
        String libelle,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutCaisse caisseStatut
) {}