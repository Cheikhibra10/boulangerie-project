package com.boulangerie.achats.dto;

import java.math.BigDecimal;

public record ReceptionLigneDto(
        Long ligneId,
        BigDecimal quantiteRecue,
        BigDecimal quantiteRefusee,
        String motifRefus
) {}