// livreurs/dto/CreationCompteRenduDto.java
package com.boulangerie.livreurs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreationCompteRenduDto {

    @NotNull(message = "Le livreur est obligatoire")
    private Long livreurId;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;
}