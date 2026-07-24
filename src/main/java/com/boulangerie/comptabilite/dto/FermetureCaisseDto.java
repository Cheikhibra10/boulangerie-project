// caisse/dto/FermetureCaisseDto.java
package com.boulangerie.comptabilite.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FermetureCaisseDto {

    @NotNull(message = "Le solde physique est obligatoire")
    @PositiveOrZero(message = "Le solde physique doit être positif ou nul")
    private BigDecimal soldePhysique;

}