package com.boulangerie.comptabilite.listener;

import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.comptabilite.service.MouvementCaisseService;
import com.boulangerie.ventes.event.VentePayeeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VentePayeeEventListener {

    private final CaisseService caisseService;
    private final MouvementCaisseService mouvementService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(VentePayeeEvent event) {

        mouvementService.enregistrerPaiement(
                event.caisseId(),
                event.montant(),
                event.libelle(),
                event.modePaiement()
        );
    }
}