package com.boulangerie.rh.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record GenererBulletinRequest(
        @NotNull Long employeId,
        @NotNull Long periodeId,
        BigDecimal retenues // optional — default 0 if null
) {}