package com.boulangerie.rh.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateEmployeRequest(
         String prenom,
         String nom,
        String telephone,
        @Email String email,
         LocalDate dateEmbauche,
        Long utilisateurId,
        Long livreurId
) {}