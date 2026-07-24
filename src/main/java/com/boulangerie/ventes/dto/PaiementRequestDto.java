package com.boulangerie.ventes.dto;

import com.boulangerie.shared.model.TypePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementRequestDto {
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private TypePaiement modePaiement;
}