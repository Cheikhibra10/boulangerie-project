package com.boulangerie.abonnements.internal;

import com.boulangerie.abonnements.api.AbonnementReportingService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AbonnementReportingServiceImpl implements AbonnementReportingService {

    private final AbonnementRepository abonnementRepository;
    private final ConsommationJournaliereRepository consommationRepository;
    private final PaiementAbonnementRepository paiementRepository;

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

        List<PaiementAbonnement> paiements = paiementRepository.findMensuels(
                        abonnementId,
                        debut.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        fin.plusDays(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                );

        return construireRapport(
                abonnement,
                periode,
                consommations,
                paiements
        );
    }

    private ConsommationMensuelleReportDto construireRapport(
            Abonnement abonnement,
            YearMonth periode,
            List<ConsommationJournaliere> consommations,
            List<PaiementAbonnement> paiements
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

        ConsommationMensuelleReportDto report = new ConsommationMensuelleReportDto()
                        .setAbonnementId(abonnement.getId())
                        .setAbonnement(abonnement.getNom())
                        .setPeriode(periode);

        for (LigneAbonnement ligne : abonnement.getLignes()) {
            ConsommationMensuelleLigneDto row = construireLigne(
                            ligne,
                            consommationsParLigne.getOrDefault(
                                    ligne.getId(),
                                    List.of()
                            ),
                            paiementsParLigne.getOrDefault(
                                    ligne.getId(),
                                    List.of()
                            )
                    );

            report.getLignes().add(row);
            report.setQuantiteTotale(
                    report.getQuantiteTotale()
                            .add(row.getQuantiteTotale())
            );

            report.setMontantTotal(
                    report.getMontantTotal()
                            .add(row.getMontant())
            );

            report.setMontantPaye(
                    report.getMontantPaye()
                            .add(row.getMontantPaye())
            );

            report.setReliquatTotal(
                    report.getReliquatTotal()
                            .add(row.getReliquat())
            );
        }

        return report;
    }

    private ConsommationMensuelleLigneDto construireLigne(
            LigneAbonnement ligne,
            List<ConsommationJournaliere> consommations,
            List<PaiementAbonnement> paiements
    ) {

        ConsommationMensuelleLigneDto dto = new ConsommationMensuelleLigneDto()
                        .setLigneId(ligne.getId())
                        .setClientId(ligne.getClient().getId())
                        .setNom(ligne.getClient().getNom())
                        .setPrenom(ligne.getClient().getPrenom())
                        .setPrixUnitaire(ligne.getPrixUnitaire())
                        .setReliquat(ligne.getReliquat());

        ajouterConsommations(dto, consommations);

        dto.setQuantiteTotale(calculerQuantite(consommations));

        dto.setMontant(calculerMontant(consommations));
        dto.setMontantPaye(calculerPaiements(paiements));
        return dto;
    }

    private void ajouterConsommations(
            ConsommationMensuelleLigneDto dto,
            List<ConsommationJournaliere> consommations
    ) {
        for (ConsommationJournaliere consommation : consommations) {
            dto.getConsommations().put(
                    consommation.getDate().getDayOfMonth(),
                    consommation.getQuantite()
            );
        }

    }

    private BigDecimal calculerMontant(
            List<ConsommationJournaliere> consommations
    ) {
        return consommations.stream()
                .map(ConsommationJournaliere::calculerMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculerPaiements(List<PaiementAbonnement> paiements) {
        return paiements.stream()
                .map(PaiementAbonnement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculerQuantite(List<ConsommationJournaliere> consommations) {
        return consommations.stream()
                .map(ConsommationJournaliere::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}