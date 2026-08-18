package com.boulangerie.abonnements.dto;

import java.time.LocalDate;

public record AbonnementDateKey(
        Long abonnementId,
        LocalDate date
) {
}