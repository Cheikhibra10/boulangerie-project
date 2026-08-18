package com.boulangerie.abonnements.dto;

import java.time.LocalDate;

public record LigneDateKey(
        Long ligneId,
        LocalDate date
) {
}