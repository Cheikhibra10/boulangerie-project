package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.event.PaiementAbonnementEnregistreEvent;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.model.PaiementAbonnement;
import com.boulangerie.abonnements.repository.PaiementAbonnementRepository;
import com.boulangerie.shared.model.TypePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class PaiementAbonnementService {

    private final PaiementAbonnementRepository paiementRepository;
    private final PaiementAbonnementFactory paiementFactory;
    private final ApplicationEventPublisher publisher;

    public PaiementAbonnement enregistrerPaiement(
            LigneAbonnement ligne,
            BigDecimal montant,
            TypePaiement modePaiement) {

        PaiementAbonnement paiement =
                paiementRepository.save(
                        paiementFactory.create(
                                ligne,
                                montant,
                                modePaiement
                        )
                );
        publisher.publishEvent(
                new PaiementAbonnementEnregistreEvent(
                        paiement.getId(),
                        ligne.getAbonnement().getId(),
                        ligne.getId(),
                        paiement.getMontant(),
                        paiement.getModePaiement(),
                        paiement.getLibelle(),
                        Instant.now()
                )
        );
        return paiement;
    }
}