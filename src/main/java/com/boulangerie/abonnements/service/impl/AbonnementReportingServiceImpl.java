package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.api.AbonnementConsommationReportDto;
import com.boulangerie.abonnements.service.AbonnementReportingService;
import com.boulangerie.abonnements.api.ConsommationMensuelleLigneDto;
import com.boulangerie.abonnements.api.ConsommationMensuelleReportDto;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.model.ConsommationJournaliere;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.model.PaiementAbonnement;
import com.boulangerie.abonnements.repository.AbonnementRepository;
import com.boulangerie.abonnements.repository.ConsommationJournaliereRepository;
import com.boulangerie.abonnements.repository.PaiementAbonnementRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AbonnementReportingServiceImpl implements AbonnementReportingService {

    private final AbonnementRepository abonnementRepository;
    private final ConsommationJournaliereRepository consommationRepository;
    private final PaiementAbonnementRepository paiementRepository;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Dakar");
    @Override
    public ConsommationMensuelleReportDto genererRapportMensuel(
            Long abonnementId,
            YearMonth periode
    ) {

        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Abonnement introuvable : " + abonnementId));

        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        List<ConsommationJournaliere> consommations = consommationRepository.findMensuelles(
                        abonnementId,
                        debut,
                        fin
                );

        Instant debutInstant = periode.atDay(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant finInstant = periode.plusMonths(1).atDay(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        List<PaiementAbonnement> paiements =
                paiementRepository
                        .findByAbonnementIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                                abonnementId,
                                debutInstant,
                                finInstant
                        );


        AbonnementConsommationReportDto abonnementConsommationReportDto =
                construireAbonnementReport(
                abonnement,
                consommations,
                paiements,
                periode
        );
        return construireRapportGlobal(periode, abonnementConsommationReportDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsommationMensuelleReportDto genererRapportMensuel(
            YearMonth periode
    ) {
        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        List<Abonnement> abonnements =
                abonnementRepository.findAbonnementsActifsPourPeriode(
                        debut,
                        fin
                );

        if (abonnements.isEmpty()) {
            return new ConsommationMensuelleReportDto()
                    .setPeriode(periode);
        }

        List<Long> abonnementIds = abonnements.stream()
                .map(Abonnement::getId)
                .toList();

        List<ConsommationJournaliere> consommations =
                consommationRepository
                        .findByAbonnementIdInAndDateBetween(
                                abonnementIds,
                                debut,
                                fin
                        );

        Instant debutInstant =
                debut.atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

        Instant finInstant =
                fin.plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();

        List<PaiementAbonnement> paiements =
                paiementRepository
                        .findByAbonnementIdInAndCreatedAtBetween(
                                abonnementIds,
                                debutInstant,
                                finInstant
                        );

        Map<Long, List<ConsommationJournaliere>> consommationsParAbonnement =
                consommations.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getLigne()
                                        .getAbonnement()
                                        .getId()
                        ));

        Map<Long, List<PaiementAbonnement>> paiementsParAbonnement =
                paiements.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getLigne()
                                        .getAbonnement()
                                        .getId()
                        ));

        List<AbonnementConsommationReportDto> rapportsAbonnements =
                abonnements.stream()
                        .map(abonnement ->
                                construireAbonnementReport(
                                        abonnement,
                                        consommationsParAbonnement.getOrDefault(
                                                abonnement.getId(),
                                                List.of()
                                        ),
                                        paiementsParAbonnement.getOrDefault(
                                                abonnement.getId(),
                                                List.of()
                                        ),
                                        periode
                                )
                        )
                        .toList();

        return construireRapportGlobal(
                periode,
                rapportsAbonnements
        );
    }

    private ConsommationMensuelleReportDto construireRapportGlobal(
            YearMonth periode,
            List<AbonnementConsommationReportDto> abonnements
    ) {
        ConsommationMensuelleReportDto report =
                new ConsommationMensuelleReportDto()
                        .setPeriode(periode)
                        .setAbonnements(
                                new ArrayList<>(abonnements)
                        );

        for (AbonnementConsommationReportDto abonnement : abonnements) {
            ajouterTotaux(report, abonnement);
        }

        return report;
    }

    private ConsommationMensuelleReportDto construireRapportGlobal(
            YearMonth periode,
            AbonnementConsommationReportDto abonnementReport
    ) {
        return new ConsommationMensuelleReportDto()
                .setPeriode(periode)
                .setAbonnements(new ArrayList<>(List.of(abonnementReport)))
                .setQuantiteTotale(abonnementReport.getQuantiteTotale())
                .setMontantMensuelTotal(abonnementReport.getMontantMensuelTotal())
                .setMontantVerseTotal(abonnementReport.getMontantVerseTotal())
                .setReliquatTotal(abonnementReport.getReliquatTotal());
    }

    private AbonnementConsommationReportDto construireAbonnementReport(
            Abonnement abonnement,
            List<ConsommationJournaliere> consommations,
            List<PaiementAbonnement> paiements,
            YearMonth periode
    ) {
        Map<Long, List<ConsommationJournaliere>> consommationsParLigne =
                consommations.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getLigne().getId()
                        ));

        Map<Long, List<PaiementAbonnement>> paiementsParLigne =
                paiements.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getLigne().getId()
                        ));

        AbonnementConsommationReportDto report =
                new AbonnementConsommationReportDto()
                        .setAbonnementId(abonnement.getId())
                        .setAbonnementNom(abonnement.getNom());

        for (LigneAbonnement ligne : abonnement.getLignes()) {

            ConsommationMensuelleLigneDto ligneReport =
                    construireLigne(
                            ligne,
                            periode,
                            consommationsParLigne.getOrDefault(
                                    ligne.getId(),
                                    List.of()
                            ),
                            paiementsParLigne.getOrDefault(
                                    ligne.getId(),
                                    List.of()
                            )
                    );

            report.getLignes().add(ligneReport);

            report.setQuantiteTotale(
                    report.getQuantiteTotale()
                            .add(ligneReport.getQuantiteTotale())
            );

            report.setMontantMensuelTotal(
                    report.getMontantMensuelTotal()
                            .add(ligneReport.getMontant())
            );

            report.setMontantVerseTotal(
                    report.getMontantVerseTotal()
                            .add(ligneReport.getMontantPaye())
            );

            report.setReliquatTotal(
                    report.getReliquatTotal()
                            .add(ligneReport.getReliquat())
            );
        }

        return report;
    }

    private ConsommationMensuelleLigneDto construireLigne(
            LigneAbonnement ligne,
            YearMonth periode,
            List<ConsommationJournaliere> consommations,
            List<PaiementAbonnement> paiements
    ) {

        BigDecimal quantiteTotale =
                consommations.stream()
                        .map(ConsommationJournaliere::getQuantite)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montant = quantiteTotale.multiply(ligne.getPrixUnitaire());

        BigDecimal montantPaye = paiements.stream()
                .map(PaiementAbonnement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ConsommationMensuelleLigneDto dto = new ConsommationMensuelleLigneDto()
                        .setLigneId(ligne.getId())
                        .setClientId(ligne.getClient().getId())
                        .setNom(ligne.getClient().getNom())
                        .setPrenom(ligne.getClient().getPrenom())
                        .setPrixUnitaire(ligne.getPrixUnitaire())
                        .setQuantiteTotale(quantiteTotale)
                        .setMontant(montant)
                        .setMontantPaye(montantPaye)
                        .setReliquat(ligne.getReliquat());

        dto.initialiserConsommations(periode);
        consommations.forEach(dto::ajouterConsommation);

        return dto;
    }

    private void ajouterTotaux(
            ConsommationMensuelleReportDto report,
            AbonnementConsommationReportDto abonnement
    ) {
        report.setQuantiteTotale(
                report.getQuantiteTotale()
                        .add(abonnement.getQuantiteTotale())
        );

        report.setMontantMensuelTotal(
                report.getMontantMensuelTotal()
                        .add(abonnement.getMontantMensuelTotal())
        );

        report.setMontantVerseTotal(
                report.getMontantVerseTotal()
                        .add(abonnement.getMontantVerseTotal())
        );

        report.setReliquatTotal(
                report.getReliquatTotal()
                        .add(abonnement.getReliquatTotal())
        );
    }
}