package com.boulangerie.achats.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.TypePaiement;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaiementFournisseurRequestDto extends AbstractAuditingDto {
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private TypePaiement modePaiement;
}