// abonnements/exception/AbonnementExpireException.java
package com.boulangerie.abonnements.exception;

import com.boulangerie.shared.exception.BusinessException;

public class AbonnementExpireException extends BusinessException {

    public AbonnementExpireException(Long abonnementId) {
        super("L'abonnement " + abonnementId + " est expiré ou pas encore commencé");
    }
}