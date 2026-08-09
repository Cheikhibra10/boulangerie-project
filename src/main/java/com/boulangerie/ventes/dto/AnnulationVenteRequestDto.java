package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnulationVenteRequestDto {

    @NotBlank
    private String motif;
}