package com.boulangerie.livreurs.service;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.livreurs.exception.CommissionNotFoundException;
import com.boulangerie.livreurs.model.CommissionRegle;
import com.boulangerie.livreurs.repository.CommissionRegleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommissionCalculator {

    private final CommissionRegleRepository repository;

    public BigDecimal calculate(Long livreurId, Long produitId, LocalDate date) {

        return repository.findActiveRule(livreurId, produitId, date)
                .map(CommissionRegle::getMontantParUnite)
                .orElseThrow(() -> new CommissionNotFoundException(livreurId, produitId, date)
                );
    }
}