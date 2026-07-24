package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.model.Periode;

public interface PeriodeService {
   Periode getPeriodeById(Long id);
   Periode getPeriodeOuverte();
    void verifierPeriodeExiste(Long periodeId);
}
