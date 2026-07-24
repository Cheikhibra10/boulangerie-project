package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.ConflictException;

public class AchatSansLigneException extends ConflictException{
    public AchatSansLigneException(Long achatId) {
        super("Impossible de réceptionner un achat sans ligne" +achatId);
    }
}