// ventes/dto/PaiementDto.java
package com.boulangerie.ventes.dto;

import com.boulangerie.shared.model.TypePaiement;
import lombok.Data;


import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaiementDto {
    private Long id;
    private BigDecimal montant;
    private TypePaiement modePaiement;
    private Long venteId;
}