package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.ConflictException;

public class AchatAnnuleException extends ConflictException {
    public AchatAnnuleException(Long achatId) {
        super("Cet achat est annulé " +achatId);
    }
}