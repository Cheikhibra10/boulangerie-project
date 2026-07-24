package com.boulangerie.abonnements.internal;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.abonnements.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class AbonnementStatisticsApiImpl implements AbonnementStatisticsApi {

    private final AbonnementRepository repository;

    @Override
    public BigDecimal calculerCA(LocalDate debut, LocalDate fin) {
        return repository.sumCaBetweenDates(debut, fin);
    }

    @Override
    public Integer calculerNActif() {
        return repository.countByActifTrue();
    }

    @Override
    public BigDecimal calculerCC() {
        return repository.sumCreditsClients();
    }
}