package com.boulangerie.reporting.service.impl;

import com.boulangerie.abonnements.dto.AbonnementConsommationReportDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleLigneDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.reporting.service.CsvExportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Objects;

@Service
public class CsvExportServiceImpl implements CsvExportService {

    private static final String HEADER = "abonnementId;clientId;date;quantite";

    private static final String SEPARATOR = ";";

    private static final String LINE_SEPARATOR = "\r\n";

    @Override
    public byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    ) {
        Objects.requireNonNull(
                report,
                "Le rapport mensuel est obligatoire"
        );

        StringBuilder csv = new StringBuilder();

        /*
         * BOM UTF-8.
         * Permet notamment à Excel de détecter correctement
         * les caractères accentués.
         */
        csv.append('\uFEFF');

        csv.append(HEADER)
                .append(LINE_SEPARATOR);

        for (AbonnementConsommationReportDto abonnement
                : report.getAbonnements()) {

            écrireAbonnement(
                    csv,
                    abonnement,
                    report.getPeriode()
            );
        }

        return csv.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private void écrireAbonnement(
            StringBuilder csv,
            AbonnementConsommationReportDto abonnement,
            YearMonth periode
    ) {

        for (ConsommationMensuelleLigneDto ligne : abonnement.getLignes()) {
            if (ligne.getConsommations() == null) {
                continue;
            }

            for (Map.Entry<Integer, BigDecimal> entry : ligne.getConsommations().entrySet()) {

                Integer jour = entry.getKey();

                BigDecimal quantite = entry.getValue();
                /*
                 * On n'exporte pas les jours sans consommation.
                 */
                if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                LocalDate date = periode.atDay(jour);

                écrireLigne(
                        csv,
                        abonnement.getAbonnementId(),
                        ligne.getClientId(),
                        date,
                        quantite
                );
            }
        }
    }

    private void écrireLigne(
            StringBuilder csv,
            Long abonnementId,
            Long clientId,
            LocalDate date,
            BigDecimal quantite
    ) {
        csv.append(abonnementId)
                .append(SEPARATOR)

                .append(clientId)
                .append(SEPARATOR)

                .append(date)
                .append(SEPARATOR)

                .append(quantite.toPlainString())

                .append(LINE_SEPARATOR);
    }
}