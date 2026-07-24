package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.ConflictException;

public class AchatDejaPayeException extends ConflictException {
    public AchatDejaPayeException(Long achatId){
        super("Cet achat a déjà été annulé "+achatId);
    }
}
