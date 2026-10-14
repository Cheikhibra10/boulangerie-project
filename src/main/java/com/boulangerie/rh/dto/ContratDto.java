package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutContrat;
import com.boulangerie.rh.model.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

// --- Contrat ---
public record ContratDto(
        Long id,
        Long employeId,
        String employeNomComplet,
        TypeContrat type,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal salaireBase,
        String poste,
        StatutContrat statut
) {}

