package com.boulangerie.rh.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAvanceRequest(
        @NotNull Long employeId,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @NotNull LocalDate dateDemande
) {
}
