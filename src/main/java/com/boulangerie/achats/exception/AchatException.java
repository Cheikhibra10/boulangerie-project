package com.boulangerie.achats.exception;

import java.util.Optional;

public abstract class AchatException extends RuntimeException {
    private final Long achatId;

    protected AchatException(Long achatId, String message) {
        super(String.format("Achat %d: %s", achatId, message));
        this.achatId = achatId;
    }

    protected AchatException(String message) {
        super(message);
        this.achatId = null;
    }

    public Optional<Long> getAchatId() {
        return Optional.ofNullable(achatId);
    }
}