package com.boulangerie.comptabilite.exception;

import java.util.Optional;

public abstract class PeriodeException extends RuntimeException {
    private final Long periodeId;
    
    protected PeriodeException(Long periodeId, String message) {
        super(String.format("Période %d: %s", periodeId, message));
        this.periodeId = periodeId;
    }
    
    protected PeriodeException(String message) {
        super(message);
        this.periodeId = null;
    }
    
    public Optional<Long> getPeriodeId() {
        return Optional.ofNullable(periodeId);
    }
}