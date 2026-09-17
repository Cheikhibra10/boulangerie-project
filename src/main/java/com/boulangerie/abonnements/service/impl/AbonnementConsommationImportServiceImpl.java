package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.dto.*;
import com.boulangerie.abonnements.exception.AbonnementExpireException;
import com.boulangerie.abonnements.exception.ConsommationDejaExistanteException;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.model.ImportAction;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.repository.AbonnementRepository;
import com.boulangerie.abonnements.repository.LigneAbonnementRepository;
import com.boulangerie.abonnements.service.AbonnementConsommationImportService;
import com.boulangerie.abonnements.service.AbonnementService;
import com.boulangerie.production.api.DistributionService;
import com.boulangerie.production.exception.DepassementRepartitionException;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbonnementConsommationImportServiceImpl implements AbonnementConsommationImportService {

    private final AbonnementService abonnementService;

    @Override
    @Transactional
    public ImportConsommationStats importer(
            List<ImportConsommationCommand> commandes
    ) {
        ImportConsommationStats stats = new ImportConsommationStats();

        for (ImportConsommationCommand command : commandes) {

            ConsommationDto dto =
                    new ConsommationDto()
                            .setDate(command.getDate())
                            .setQuantite(command.getQuantite());

            ImportAction action = abonnementService.synchroniserConsommation(
                            command.getLigneId(),
                            dto
                    );

            stats.setLignesTraitees(
                    stats.getLignesTraitees() + 1
            );

            switch (action) {

                case CREEE ->
                        stats.setConsommationsCreees(
                                stats.getConsommationsCreees() + 1
                        );

                case MODIFIEE ->
                        stats.setConsommationsModifiees(
                                stats.getConsommationsModifiees() + 1
                        );

                case INCHANGEE ->
                        stats.setConsommationsInchangees(
                                stats.getConsommationsInchangees() + 1
                        );
            }
        }

        return stats;
    }

//    private void validerCommande(ImportConsommationCommand command) {
//
//        if (command == null) {
//            throw new BadRequestException("Commande d'import invalide.");
//        }
//
//        if (command.getAbonnementId() == null) {
//            throw new BadRequestException("abonnementId obligatoire.");
//        }
//
//        if (command.getClientId() == null) {
//            throw new BadRequestException("clientId obligatoire.");
//        }
//
//        if (command.getDate() == null) {
//            throw new BadRequestException("La date de consommation est obligatoire.");
//        }
//
//        if (command.getQuantite() == null || command.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
//            throw new BadRequestException("La quantité doit être strictement positive.");
//        }
//
//        LigneAbonnement ligne = ligneRepository
//                .findById(command.getLigneId())
//                .orElseThrow(() ->
//                        new EntityNotFoundException("LigneAbonnement introuvable : " + command.getLigneId())
//                );
//
//        /*
//         * Vérification forte de l'identité.
//         */
//        if (!Objects.equals(ligne.getClient().getId(), command.getClientId())) {
//            throw new BadRequestException("Le client ne correspond pas à la ligne d'abonnement.");
//        }
//
//        if (!Objects.equals(ligne.getAbonnement().getId(), command.getAbonnementId())) {
//            throw new BadRequestException("La ligne n'appartient pas à l'abonnement indiqué.");
//        }
//
//        Abonnement abonnement = ligne.getAbonnement();
//
//        if (!abonnement.estValidePour(command.getDate())) {
//            throw new AbonnementExpireException(abonnement.getId());
//        }
//
//        /*
//         * Important :
//         * on vérifie également la quantité distribuée
//         * avant toute écriture.
//         */
//        BigDecimal quantiteDistribuee = distributionService.getQuantiteDistribuee(
//                        abonnement.getId(),
//                        command.getDate()
//                );
//
//        abonnement.verifierQuantiteDisponible(
//                command.getDate(),
//                command.getQuantite(),
//                quantiteDistribuee
//        );
//
//        /*
//         * Empêche l'import de créer un doublon.
//         */
//        boolean existe = ligne.getConsommations()
//                .stream()
//                .anyMatch(c ->
//                        command.getDate().equals(c.getDate())
//                );
//        if (existe) {
//            throw new ConsommationDejaExistanteException(ligne.getId(), command.getDate());
//        }
//    }
//
//    private void validerQuantitesGlobales(
//            List<ImportConsommationCommand> commandes
//    ) {
//
//        Map<AbonnementDateKey, BigDecimal> quantitesParJour =
//                commandes.stream()
//                        .collect(Collectors.groupingBy(
//                                command -> new AbonnementDateKey(
//                                        command.getAbonnementId(),
//                                        command.getDate()
//                                ),
//                                Collectors.reducing(
//                                        BigDecimal.ZERO,
//                                        ImportConsommationCommand::getQuantite,
//                                        BigDecimal::add
//                                )
//                        ));
//
//        for (Map.Entry<AbonnementDateKey, BigDecimal> entry : quantitesParJour.entrySet()) {
//            AbonnementDateKey key = entry.getKey();
//            BigDecimal quantiteImportee = entry.getValue();
//            BigDecimal quantiteDistribuee = distributionService.getQuantiteDistribuee(
//                            key.abonnementId(),
//                            key.date()
//                    );
//
//            if (quantiteImportee.compareTo(quantiteDistribuee) > 0) {
//                throw new DepassementRepartitionException(
//                        key.abonnementId(),
//                        quantiteDistribuee,
//                        quantiteImportee
//                );
//            }
//        }
//    }
//
//    private void verifierAbsenceDoublon(
//            List<ImportConsommationCommand> commandes
//    ) {
//
//        Set<LigneDateKey> dates =
//                new HashSet<>();
//
//        for (ImportConsommationCommand command : commandes) {
//
//            LigneDateKey key =
//                    new LigneDateKey(
//                            command.getLigneId(),
//                            command.getDate()
//                    );
//
//            if (!dates.add(key)) {
//
//                throw new BadRequestException(
//                        "Plusieurs consommations sont présentes "
//                                + "pour le client "
//                                + command.getClientId()
//                                + " à la date "
//                                + command.getDate()
//                );
//            }
//        }
//    }
}