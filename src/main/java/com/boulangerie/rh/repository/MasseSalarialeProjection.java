package com.boulangerie.rh.repository;

import java.math.BigDecimal;

public interface MasseSalarialeProjection {
    long getNombre();
    BigDecimal getTotalBrut();
    BigDecimal getTotalRetenues();
    BigDecimal getTotalNet();
}