package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutEmploye;

public record EmployeFilter(
        String nom,
        String prenom,
        String matricule,
        StatutEmploye statut
) {}