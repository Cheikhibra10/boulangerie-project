package com.boulangerie.comptabilite.scheduling;

import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.comptabilite.event.PeriodeAutomatiqueEvent;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.comptabilite.service.PeriodeClosureService;
import com.boulangerie.comptabilite.service.PeriodeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodeSchedulingService {

    private final PeriodeRepository periodeRepository;
    private final PeriodeManagementService periodeManagementService;
    private final PeriodeClosureService periodeClosureService;
    private final ApplicationEventPublisher publisher;

    @Scheduled(
            cron = "${boulangerie.periode.cron:0 5 0 1 * *}",
            zone = "${boulangerie.periode.timezone:Africa/Dakar}"
    )
    public void executerCycleMensuel() {

        log.info("Démarrage du cycle mensuel automatique des périodes");

        if (cloturerPeriodeOuverteSiPresente()) {
            ouvrirPeriodeDuMoisSiAbsente();
        } else {
            log.warn("Cycle mensuel interrompu : la clôture automatique a échoué — " +
                    "la nouvelle période ne sera pas ouverte automatiquement ce mois-ci.");
        }
    }

    public void executerManuellement() {
        log.info("Exécution manuelle du cycle mensuel des périodes");
        executerCycleMensuel();
    }

    private boolean cloturerPeriodeOuverteSiPresente() {

        Optional<Periode> ouverte = periodeRepository.findByStatut(StatutPeriode.OUVERTE);

        if (ouverte.isEmpty()) {
            log.info("Aucune période ouverte à clôturer — rien à faire côté clôture.");
            return true;
        }

        Long id = ouverte.get().getId();

        try {
            periodeManagementService.fermerPeriode(id);
            periodeClosureService.cloturerPeriode(id);

            log.info("Période #{} clôturée automatiquement", id);

            publisher.publishEvent(new PeriodeAutomatiqueEvent(
                    id, true, "Période #" + id + " clôturée automatiquement"
            ));

            return true;
        } catch (Exception ex) {
            log.error("Échec de la clôture automatique de la période #{} : {}", id, ex.getMessage(), ex);

            publisher.publishEvent(new PeriodeAutomatiqueEvent(
                    id, false,
                    "⚠️ Échec de la clôture automatique de la période #" + id
                            + " — intervention manuelle requise (" + ex.getMessage() + ")"
            ));

            return false;
        }
    }

    private void ouvrirPeriodeDuMoisSiAbsente() {

        YearMonth moisCourant = YearMonth.now();

        if (periodeRepository.existsByDateDebut(moisCourant.atDay(1))) {
            log.info("Une période pour {} existe déjà — aucune création automatique nécessaire.", moisCourant);
            return;
        }

        try {
            var nouvelle = periodeManagementService.creerPeriodeMensuelle(moisCourant);

            log.info("Période #{} ouverte automatiquement pour {}", nouvelle.getId(), moisCourant);

            publisher.publishEvent(new PeriodeAutomatiqueEvent(
                    nouvelle.getId(), true,
                    "Nouvelle période ouverte automatiquement : " + moisCourant
            ));
        } catch (Exception ex) {
            log.error("Échec de l'ouverture automatique de la période pour {} : {}", moisCourant, ex.getMessage(), ex);

            publisher.publishEvent(new PeriodeAutomatiqueEvent(
                    null, false,
                    "⚠️ Échec de l'ouverture automatique de la période pour " + moisCourant
                            + " — intervention manuelle requise"
            ));
        }
    }
}