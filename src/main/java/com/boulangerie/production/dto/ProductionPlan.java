package com.boulangerie.production.dto;

import com.boulangerie.shared.dto.ConsommationIngredient;

import java.math.BigDecimal;
import java.util.List;

public record ProductionPlan(

        BigDecimal quantitePrevue,

        List<ConsommationIngredient> consommations

) {}