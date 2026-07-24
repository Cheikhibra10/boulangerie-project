package com.boulangerie.comptabilite.internal;

import com.boulangerie.comptabilite.api.FinanceStatistics;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class FinanceStatisticsImpl implements FinanceStatistics {

    private final MouvementCaisseRepository repository;

    @Override
    public BigDecimal calculerTotalCharges(LocalDate debut,
                                           LocalDate fin) {

        return repository.sumChargesBetweenDates(debut, fin);
    }
}