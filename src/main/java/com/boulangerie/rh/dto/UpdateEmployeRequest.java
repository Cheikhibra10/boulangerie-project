package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutEmploye;

import java.time.LocalDate;

public record UpdateEmployeRequest(
        String nom,
        String prenom,
        String telephone,
        String email,
        LocalDate dateSortie,
        StatutEmploye statut,
        Long utilisateurId,
        Long livreurId
) {}