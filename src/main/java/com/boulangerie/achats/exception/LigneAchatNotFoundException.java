package com.boulangerie.achats.exception;

import com.boulangerie.shared.exception.ConflictException;

public class LigneAchatNotFoundException extends ConflictException {
    public LigneAchatNotFoundException(Long ligneId, Long achatId) {
        super(String.format("Ligne %d non trouvée dans l'achat %d", ligneId,  achatId));
    }
}