package com.boulangerie.livreurs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CommissionRegleRequest {

    @NotNull
    private Long livreurId;

    @NotNull
    private Long produitId;

    @NotNull
    @Positive
    private BigDecimal montantParUnite;

    @NotNull
    private LocalDate dateDebut;

}