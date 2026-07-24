package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.BusinessException;

public class AbonnementInactifException extends BusinessException {
    public AbonnementInactifException(Long abonnementId) {
        super("L'abonnement " + abonnementId + " n'est pas actif");
    }
}
