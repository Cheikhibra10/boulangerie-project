package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class PeriodeDejaClotureeException extends PeriodeException {
    public PeriodeDejaClotureeException(Long periodeId) {
        super(periodeId, "La période est déjà clôturée");
    }
}