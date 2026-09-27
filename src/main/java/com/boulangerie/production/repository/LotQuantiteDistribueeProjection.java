package com.boulangerie.production.repository;

import java.math.BigDecimal;

public interface LotQuantiteDistribueeProjection {
        Long getLotId();
        BigDecimal getTotal();
    }