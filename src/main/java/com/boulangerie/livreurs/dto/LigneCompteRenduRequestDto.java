// livreurs/dto/LigneCompteRenduRequestDto.java
package com.boulangerie.livreurs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class LigneCompteRenduRequestDto {

    @NotNull(message = "La destination de production est obligatoire")
    private Long destinationProductionId;

    @NotNull(message = "La quantité livrée est obligatoire")
    @Positive(message = "La quantité livrée doit être positive")
    private BigDecimal qteLivree;

    @PositiveOrZero(message = "La quantité pour abonnement doit être positive ou nulle")
    private BigDecimal qteAbonnement = BigDecimal.ZERO;

    @PositiveOrZero(message = "Les retours doivent être positifs ou nuls")
    private BigDecimal retours = BigDecimal.ZERO;

    @PositiveOrZero(message = "La quantité offerte doit être positive ou nulle")
    private BigDecimal qteOfferte = BigDecimal.ZERO;

    @PositiveOrZero(message = "Les dépenses du livreur doivent être positives ou nulles")
    private BigDecimal depensesLivreur = BigDecimal.ZERO;
}