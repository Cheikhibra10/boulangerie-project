package com.boulangerie.ventes.dto;

import com.boulangerie.ventes.model.StatutVente;

import java.time.LocalDate;

public record VenteBoutiqueFilter(
        StatutVente statut,
        String utilisateurNom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}