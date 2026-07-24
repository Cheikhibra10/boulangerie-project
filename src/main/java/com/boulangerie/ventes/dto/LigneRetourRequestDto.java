package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneRetourRequestDto {

    @NotNull
    private Long produitId;

    @NotNull
    @Positive
    private BigDecimal quantite;
}