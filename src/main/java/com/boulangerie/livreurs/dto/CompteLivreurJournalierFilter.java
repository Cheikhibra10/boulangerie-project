package com.boulangerie.livreurs.dto;

import com.boulangerie.livreurs.model.StatutCompteRendu;

import java.time.LocalDate;

public record CompteLivreurJournalierFilter(
        StatutCompteRendu statut,
        String livreurNom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}