package com.boulangerie.rh.dto;

import java.math.BigDecimal;

public record MasseSalarialeAggregate(
        long nombre,
        BigDecimal totalBrut,
        BigDecimal totalRetenues,
        BigDecimal totalNet
) {}