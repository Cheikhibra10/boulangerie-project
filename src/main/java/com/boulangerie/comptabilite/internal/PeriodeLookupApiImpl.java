package com.boulangerie.comptabilite.internal;

import com.boulangerie.comptabilite.api.PeriodeLookupApi;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class PeriodeLookupApiImpl implements PeriodeLookupApi {
    private final PeriodeRepository periodeRepository;
    @Override
    public Long findPeriodeIdOrThrow(Long id) {
        if (!periodeRepository.existsById(id)) {
            throw new EntityNotFoundException("Periode introuvable : " + id);
        }
        return id;
    }
}