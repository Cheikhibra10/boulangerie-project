package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutBulletin;

import java.math.BigDecimal;
import java.time.LocalDate;

// --- Bulletin ---
public record BulletinPaieDto(
        Long id,
        Long employeId,
        String employeNomComplet,
        Long contratId,
        Long periodeId,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        BigDecimal salaireBrut,
        BigDecimal retenues,
        BigDecimal netAPayer,
        BigDecimal joursTravailles,
        BigDecimal joursAbsents,
        StatutBulletin statut,
        LocalDate datePaiement
) {}
