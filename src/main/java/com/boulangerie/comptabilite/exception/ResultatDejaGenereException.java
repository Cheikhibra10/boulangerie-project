package com.boulangerie.comptabilite.exception;

import com.boulangerie.shared.exception.ConflictException;

public class ResultatDejaGenereException extends PeriodeException {
    public ResultatDejaGenereException(Long periodeId) {
        super(periodeId, "Un résultat a déjà été généré pour cette période");
    }
}