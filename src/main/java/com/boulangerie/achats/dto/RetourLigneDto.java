package com.boulangerie.achats.dto;

import java.math.BigDecimal;

public record RetourLigneDto(

        Long ligneId,

        BigDecimal quantiteRetournee,

        String motif

) {}