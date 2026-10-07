package com.boulangerie.production.dto;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.EtatPain;

import java.time.LocalDate;

public record DestinationProductionFilter(
        CanalDistribution canal,
        EtatPain etatPain,
        String produitLibelle,
        String livreurNom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}