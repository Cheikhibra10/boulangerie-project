package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.dto.LigneMontantDto;
import com.boulangerie.comptabilite.dto.VersementsJourDto;
import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;
import com.boulangerie.comptabilite.service.VersementsCsvExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Export CSV du rapport mensuel VERSEMENTS/FRAIS.
 *
 * Même structure en blocs journaliers de hauteur variable que
 * l'export Excel (VersementsExcelExportServiceImpl), mais avec les
 * totaux journaliers et mensuels calculés en Java plutôt qu'en
 * formules — le CSV n'a pas de moteur de calcul.
 */
@Service
@RequiredArgsConstructor
public class VersementsCsvExportServiceImpl
        implements VersementsCsvExportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SEPARATOR = ";";

    private static final String LINE_SEPARATOR = "\r\n";

    @Override
    public byte[] exporterVersementsMensuel(
            VersementsRapportMensuelDto rapport
    ) {

        Objects.requireNonNull(
                rapport,
                "Le rapport de versements est obligatoire."
        );

        StringBuilder csv = new StringBuilder();

        /*
         * BOM UTF-8 : permet à Excel de détecter correctement les
         * caractères accentués dans les libellés (crédit, reliquat,
         * améliorant...).
         */
        csv.append('\uFEFF');

        csv.append(csvField("Date")).append(SEPARATOR)
                .append(csvField("Versement")).append(SEPARATOR)
                .append(csvField("Montant versement")).append(SEPARATOR)
                .append(csvField("Frais")).append(SEPARATOR)
                .append(csvField("Montant frais"))
                .append(LINE_SEPARATOR);

        BigDecimal sommeVersements = BigDecimal.ZERO;
        BigDecimal sommeFrais = BigDecimal.ZERO;

        for (VersementsJourDto jour : rapport.getJours()) {

            List<LigneMontantDto> versements = jour.getVersements();
            List<LigneMontantDto> frais = jour.getFrais();

            int nbLignes = Math.max(versements.size(), frais.size());

            if (nbLignes == 0) {
                continue;
            }

            BigDecimal totalJourVersements = sommer(versements);
            BigDecimal totalJourFrais = sommer(frais);

            sommeVersements = sommeVersements.add(totalJourVersements);
            sommeFrais = sommeFrais.add(totalJourFrais);

            String dateFormattee = DATE_FORMAT.format(jour.getDate());

            for (int i = 0; i < nbLignes; i++) {

                String versementLibelle = "";
                String versementMontant = "";
                String fraisLibelle = "";
                String fraisMontant = "";

                if (i < versements.size()) {
                    LigneMontantDto ligne = versements.get(i);
                    versementLibelle = nullSafe(ligne.getLibelle());
                    versementMontant = csvField(ligne.getMontant());
                }

                if (i < frais.size()) {
                    LigneMontantDto ligne = frais.get(i);
                    fraisLibelle = nullSafe(ligne.getLibelle());
                    fraisMontant = csvField(ligne.getMontant());
                }

                csv.append(csvField(i == 0 ? dateFormattee : ""))
                        .append(SEPARATOR)
                        .append(csvField(versementLibelle))
                        .append(SEPARATOR)
                        .append(versementMontant)
                        .append(SEPARATOR)
                        .append(csvField(fraisLibelle))
                        .append(SEPARATOR)
                        .append(fraisMontant)
                        .append(LINE_SEPARATOR);
            }

            csv.append(csvField(""))
                    .append(SEPARATOR)
                    .append(csvField("Total"))
                    .append(SEPARATOR)
                    .append(csvField(totalJourVersements))
                    .append(SEPARATOR)
                    .append(csvField("Total"))
                    .append(SEPARATOR)
                    .append(csvField(totalJourFrais))
                    .append(LINE_SEPARATOR);
        }

        if (sommeVersements.signum() != 0 || sommeFrais.signum() != 0) {

            csv.append(csvField(""))
                    .append(SEPARATOR)
                    .append(csvField("SOMME TOTALE DES VERSEMENTS"))
                    .append(SEPARATOR)
                    .append(csvField(sommeVersements))
                    .append(SEPARATOR)
                    .append(csvField("SOMME TOTALE DES FRAIS"))
                    .append(SEPARATOR)
                    .append(csvField(sommeFrais))
                    .append(LINE_SEPARATOR);

            csv.append(csvField(""))
                    .append(SEPARATOR)
                    .append(csvField("Total"))
                    .append(SEPARATOR)
                    .append(csvField(sommeVersements.subtract(sommeFrais)))
                    .append(SEPARATOR)
                    .append(csvField(""))
                    .append(SEPARATOR)
                    .append(csvField(""))
                    .append(LINE_SEPARATOR);
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private BigDecimal sommer(List<LigneMontantDto> lignes) {

        BigDecimal somme = BigDecimal.ZERO;

        for (LigneMontantDto ligne : lignes) {
            somme = somme.add(safe(ligne.getMontant()));
        }

        return somme;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String csvField(BigDecimal value) {

        if (value == null) {
            return "";
        }

        return value.stripTrailingZeros().toPlainString();
    }

    private String csvField(String value) {

        if (value == null) {
            return "";
        }

        boolean needsQuoting =
                value.contains(SEPARATOR)
                        || value.contains("\"")
                        || value.contains("\n");

        if (!needsQuoting) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}