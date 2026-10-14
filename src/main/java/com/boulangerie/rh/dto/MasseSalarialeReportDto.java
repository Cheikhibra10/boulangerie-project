package com.boulangerie.rh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MasseSalarialeReportDto(
        Long periodeId,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        long nombreBulletins,
        BigDecimal totalBrut,
        BigDecimal totalRetenues,
        BigDecimal totalNet
) {}

