package com.boulangerie.comptabilite.internal;

import com.boulangerie.comptabilite.api.AccountingPeriodApi;
import com.boulangerie.comptabilite.exception.PeriodeDejaFermeeException;
import com.boulangerie.comptabilite.exception.*;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class AccountingPeriodApiImpl implements AccountingPeriodApi {

    private final PeriodeRepository periodeRepository;

    @Override
    public void verifierDateDansPeriodeOuverte(LocalDate date) {

        Periode periode = periodeRepository.findByDate(date)
                .orElseThrow(() ->
                        new PeriodeeNotFoundException(date));

        if (!periode.estOuverte()) {
            throw new PeriodeNonOuverteException(
                    periode.getId(),
                    periode.getStatut()
            );
        }
    }
}