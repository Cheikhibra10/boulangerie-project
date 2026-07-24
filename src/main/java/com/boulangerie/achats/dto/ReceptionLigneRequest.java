package com.boulangerie.achats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionLigneRequest {
    @NotNull(message = "Ligne d'achat est obligatoire")
    private Long ligneId;
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantiteRecue;

    private BigDecimal quantiteRefusee;
    private String motifRefus;
}
