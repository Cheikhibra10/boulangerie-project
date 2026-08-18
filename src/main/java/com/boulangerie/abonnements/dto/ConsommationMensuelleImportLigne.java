package com.boulangerie.abonnements.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class ConsommationMensuelleImportLigne {
    private Long abonnementId;

    private Long clientId;

    private Map<Integer, BigDecimal> consommations = new LinkedHashMap<>();
}