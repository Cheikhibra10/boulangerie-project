package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutEmploye;

import java.time.LocalDate;

public record EmployeDto(
        Long id,
        String matricule,
        String nom,
        String prenom,
        String telephone,
        String email,
        LocalDate dateEmbauche,
        LocalDate dateSortie,
        StatutEmploye statut,
        Long utilisateurId,
        Long livreurId
) {}