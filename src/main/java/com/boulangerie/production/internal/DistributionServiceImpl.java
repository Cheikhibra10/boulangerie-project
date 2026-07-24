package com.boulangerie.production.internal;

import com.boulangerie.production.api.DistributionService;
import com.boulangerie.production.repository.DestinationProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class DistributionServiceImpl implements DistributionService {

    private final DestinationProductionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getQuantiteDistribuee(
            Long abonnementId,
            LocalDate date
    ) {
        return repository.sumQuantiteDistribuee(
                abonnementId,
                date
        );
    }
}