package com.boulangerie.livreurs.internal;

import com.boulangerie.livreurs.api.LivreurStatisticsApi;
import com.boulangerie.livreurs.repository.CompteLivreurJournalierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class LivreurStatisticsApiImpl implements LivreurStatisticsApi {

    private final CompteLivreurJournalierRepository repository;

    @Override
    public BigDecimal calculerCAVentesLivreurs(LocalDate debut, LocalDate fin) {
        return repository.sumCaLivreursBetweenDates(debut, fin);
    }

    @Override
    public BigDecimal calculerReliquatLivreurs() {
        return repository.sumSoldeActuel();
    }
}