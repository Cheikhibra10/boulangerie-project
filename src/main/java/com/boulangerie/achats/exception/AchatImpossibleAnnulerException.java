package com.boulangerie.achats.exception;

import com.boulangerie.achats.model.StatutReception;
import com.boulangerie.shared.exception.ConflictException;

public class AchatImpossibleAnnulerException extends ConflictException {
    public AchatImpossibleAnnulerException(Long achatId, StatutReception statut) {
        super(String.format("Impossible d'annuler, statut actuel: %s", statut));
    }
}