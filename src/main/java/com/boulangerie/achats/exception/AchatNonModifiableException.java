package com.boulangerie.achats.exception;

import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.StatutReception;
import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.exception.ConflictException;

public class AchatNonModifiableException extends ConflictException {
    public AchatNonModifiableException(Long achatId, StatutReception statut) {
        super(String.format("Impossible de modifier, statut actuel: %s, achatId: %d", statut, achatId));
    }
}