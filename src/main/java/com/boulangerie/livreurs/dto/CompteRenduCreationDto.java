package com.boulangerie.livreurs.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CompteRenduCreationDto {

    @NotNull
    private Long livreurId;

    @NotNull
    private LocalDate date;

    @NotEmpty
    private List<LigneCompteRenduRequestDto> lignes;
}