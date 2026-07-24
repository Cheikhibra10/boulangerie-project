package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.ConflictException;

public class AchatDejaRecuException extends ConflictException {
    public AchatDejaRecuException(Long achatId) {
        super("Cet achat a déjà été réceptionné ou payé" +achatId);
    }
}