package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutAvance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvanceSalaireDto(
        Long id,
        Long employeId,
        String employeNomComplet,
        BigDecimal montant,
        LocalDate dateDemande,
        StatutAvance statut,
        Long bulletinId
) {}

