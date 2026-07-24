package com.boulangerie.comptabilite.exception;

public class ResultatNotFoundException extends PeriodeException {
    public ResultatNotFoundException(Long periodeId) {
        super(periodeId, "Résultat introuvable pour cette période");
    }
}