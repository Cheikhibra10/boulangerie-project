package com.boulangerie.comptabilite.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialConstants {
    
    // ===== OWNERSHIP SPLIT =====
    public static final BigDecimal PART_GERANT = BigDecimal.valueOf(0.30);
    public static final BigDecimal PART_BOULANGERIE = BigDecimal.valueOf(0.70);
    
    // ===== CALCULATION CONSTANTS =====
    public static final int FINANCIAL_SCALE = 2;
    public static final RoundingMode FINANCIAL_ROUNDING = RoundingMode.HALF_UP;
    public static final int INVENTORY_SCALE = 3;
    
    // ===== THRESHOLDS =====
    public static final BigDecimal MIN_BENEFIT_THRESHOLD = BigDecimal.valueOf(0.01);
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    
    // ===== PERIOD CONSTANTS =====
    public static final int DEFAULT_SNAPSHOT_RETENTION_MONTHS = 24;
    
    private FinancialConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}