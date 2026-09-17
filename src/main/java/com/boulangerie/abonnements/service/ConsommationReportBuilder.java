package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.*;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.projection.ConsommationReportProjection;
import com.boulangerie.abonnements.projection.LigneAbonnementReportProjection;
import com.boulangerie.abonnements.projection.PaiementReportProjection;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.*;

@Component
public class ConsommationReportBuilder {

    public ConsommationMensuelleReportDto build(
            YearMonth periode,
            List<Abonnement> abonnements,
            List<LigneAbonnementReportProjection> lignes,
            List<ConsommationReportProjection> consommations,
            List<PaiementReportProjection> paiements
    ) {

        Map<Long, List<LigneAbonnementReportProjection>> lignesParAbonnement =
                lignes.stream()
                        .collect(Collectors.groupingBy(
                                LigneAbonnementReportProjection::getAbonnementId
                        ));

        Map<Long, List<ConsommationReportProjection>> consommationsParLigne =
                consommations.stream()
                        .collect(Collectors.groupingBy(
                                ConsommationReportProjection::getLigneId
                        ));

        Map<Long, List<PaiementReportProjection>> paiementsParLigne =
                paiements.stream()
                        .collect(Collectors.groupingBy(
                                PaiementReportProjection::getLigneId
                        ));

        List<AbonnementConsommationReportDto> reports =
                abonnements.stream()
                        .map(abonnement ->
                                construireAbonnementReport(
                                        abonnement,
                                        periode,
                                        lignesParAbonnement.getOrDefault(
                                                abonnement.getId(),
                                                List.of()
                                        ),
                                        consommationsParLigne,
                                        paiementsParLigne
                                )
                        )
                        .toList();

        return construireRapportGlobal(
                periode,
                reports
        );
    }

    private AbonnementConsommationReportDto construireAbonnementReport(
            Abonnement abonnement,
            YearMonth periode,
            List<LigneAbonnementReportProjection> lignes,
            Map<Long, List<ConsommationReportProjection>> consommationsParLigne,
            Map<Long, List<PaiementReportProjection>> paiementsParLigne
    ) {


        AbonnementConsommationReportDto report =
                new AbonnementConsommationReportDto()
                        .setAbonnementId(abonnement.getId())
                        .setAbonnementNom(abonnement.getNom());


        for (LigneAbonnementReportProjection ligne : lignes) {

            ConsommationMensuelleLigneDto ligneReport =
                    construireLigne(
                            ligne,
                            periode,
                            consommationsParLigne.getOrDefault(
                                    ligne.getLigneId(),
                                    List.of()
                            ),
                            paiementsParLigne.getOrDefault(
                                    ligne.getLigneId(),
                                    List.of()
                            )
                    );

            report.getLignes().add(ligneReport);

            ajouterTotaux(report, ligneReport);
        }

        return report;
    }

    private ConsommationMensuelleLigneDto construireLigne(
            LigneAbonnementReportProjection ligne,
            YearMonth periode,
            List<ConsommationReportProjection> consommations,
            List<PaiementReportProjection> paiements
    ) {

        BigDecimal quantiteTotale = consommations.stream()
                        .map(ConsommationReportProjection::getQuantite)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal montant = quantiteTotale.multiply(
                        ligne.getPrixUnitaire()
                );

        BigDecimal montantPaye = paiements.stream()
                        .map(PaiementReportProjection::getMontant)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        ConsommationMensuelleLigneDto dto = new ConsommationMensuelleLigneDto()
                        .setLigneId(ligne.getLigneId())
                        .setClientId(ligne.getClientId())
                        .setNom(ligne.getClientNom())
                        .setPrenom(ligne.getClientPrenom())
                        .setPrixUnitaire(ligne.getPrixUnitaire())
                        .setQuantiteTotale(quantiteTotale)
                        .setMontant(montant)
                        .setMontantPaye(montantPaye)
                        .setReliquat(ligne.getReliquat());

        dto.initialiserConsommations(periode);

        consommations.forEach(consommation ->
                dto.ajouterConsommation(consommation.getDate(), consommation.getQuantite())
        );

        return dto;
    }

    private void ajouterTotaux(
            AbonnementConsommationReportDto report,
            ConsommationMensuelleLigneDto ligne
    ) {

        report.setQuantiteTotale(
                report.getQuantiteTotale()
                        .add(ligne.getQuantiteTotale())
        );

        report.setMontantMensuelTotal(
                report.getMontantMensuelTotal()
                        .add(ligne.getMontant())
        );

        report.setMontantVerseTotal(
                report.getMontantVerseTotal()
                        .add(ligne.getMontantPaye())
        );

        report.setReliquatTotal(
                report.getReliquatTotal()
                        .add(ligne.getReliquat())
        );
    }

    private ConsommationMensuelleReportDto construireRapportGlobal(
            YearMonth periode,
            List<AbonnementConsommationReportDto> abonnements
    ) {

        ConsommationMensuelleReportDto report = new ConsommationMensuelleReportDto()
                        .setPeriode(periode)
                        .setAbonnements(new ArrayList<>(abonnements));

        for (AbonnementConsommationReportDto abonnement : abonnements) {
            ajouterTotaux(report, abonnement);
        }
        return report;
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