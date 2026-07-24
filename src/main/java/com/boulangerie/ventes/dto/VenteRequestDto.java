// ventes/dto/CreationVenteDto.java
package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenteRequestDto {

    @NotNull(message = "La caisse est obligatoire")
    private Long caisseId;

    @NotEmpty
    private List<LigneVenteRequestDto> lignes;

    @NotNull
    private PaiementRequestDto paiement;
}