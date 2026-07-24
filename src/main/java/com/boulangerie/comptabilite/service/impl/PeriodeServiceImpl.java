package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.comptabilite.service.PeriodeService;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PeriodeServiceImpl implements PeriodeService {
    private final PeriodeRepository repository;

    @Override
    public Periode getPeriodeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Periode introuvable" +id));
    }

    @Override
    public Periode getPeriodeOuverte() {
        return repository.findByStatut(StatutPeriode.OUVERTE)
                .orElseThrow(()-> new EntityNotFoundException("Periode ouverte introuvable: "));

    }

    @Override
    @Transactional(readOnly = true)
    public void verifierPeriodeExiste(Long periodeId) {
        if (!repository.existsById(periodeId)) {
            throw new EntityNotFoundException("Période introuvable : " + periodeId);
        }
    }

}
