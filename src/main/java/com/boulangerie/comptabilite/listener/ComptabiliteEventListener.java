package com.boulangerie.comptabilite.listener;

import com.boulangerie.abonnements.event.VersementAbonnementEnregistreEvent;
import com.boulangerie.abonnements.event.VersementEvent;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.service.*;
import com.boulangerie.livreurs.event.CompteRenduLivreurClotureEvent;
import com.boulangerie.shared.dto.MouvementCaisseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ComptabiliteEventListener {

    private final CaisseService caisseService;

    private final MouvementCaisseService mouvementService;


    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(MouvementCaisseEvent event) {

        mouvementService.creerMouvement(
                event.type(),
                event.sens(),
                event.modePaiement(),
                caisseService.findCaisseOrThrow(event.caisseId()),
                event.libelle(),
                event.montant()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CompteRenduLivreurClotureEvent event) {

        Caisse caisse = caisseService.getCaisseOuverte();
        mouvementService.creerMouvement(
                event.type(),
                event.sens(),
                event.modePaiement(),
                caisse,
                event.libelle(),
                event.montant()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(VersementAbonnementEnregistreEvent event){
        Caisse caisse = caisseService.getCaisseOuverte();
        mouvementService.creerMouvement(
                event.type(),
                event.sens(),
                event.modePaiement(),
                caisse,
                event.libelle(),
                event.montant()
        );
    }



}