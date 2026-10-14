package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.TypeContrat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;


public record CreateContratRequest(
        @NotNull Long employeId,
        @NotNull TypeContrat type,
        @NotNull LocalDate dateDebut,
        LocalDate dateFin,
        @NotNull @DecimalMin("0.01") BigDecimal salaireBase,
        String poste
) {
}
