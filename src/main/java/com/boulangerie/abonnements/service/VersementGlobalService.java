package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.event.VersementAbonnementEnregistreEvent;
import com.boulangerie.abonnements.model.CompteAbonnement;
import com.boulangerie.abonnements.model.VersementAbonnement;
import com.boulangerie.abonnements.repository.VersementAbonnementRepository;
import com.boulangerie.shared.model.TypePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

// abonnements/domain/service/VersementGlobalService.java
@Service
@RequiredArgsConstructor
@Transactional
public class VersementGlobalService {

    private final VersementAbonnementRepository repository;
    private final VersementAbonnementFactory factory;
    private final ApplicationEventPublisher publisher;

    public VersementAbonnement enregistrerVersement(
            CompteAbonnement compte,
            BigDecimal montant,
            TypePaiement modePaiement) {

        VersementAbonnement versement =
                repository.save(
                        factory.create(compte, montant, modePaiement)
                );

        publisher.publishEvent(
                new VersementAbonnementEnregistreEvent(
                        versement.getId(),
                        compte.getAbonnement().getId(),
                        compte.getId(),
                        versement.getMontant(),
                        versement.getModePaiement(),
                        versement.getLibelle(),
                        Instant.now()
                )
        );

        return versement;
    }
}