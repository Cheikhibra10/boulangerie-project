package com.boulangerie.comptabilite.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnregistrerDepenseDto {

    @NotNull
    private Long categorieId;

    @NotBlank
    private String libelle;

    @NotNull
    @Positive
    private BigDecimal montant;
}