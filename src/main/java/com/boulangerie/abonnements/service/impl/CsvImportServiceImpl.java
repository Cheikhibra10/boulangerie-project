package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.dto.ConsommationImportResultDto;
import com.boulangerie.abonnements.dto.ImportConsommationCommand;
import com.boulangerie.abonnements.dto.ImportConsommationStats;
import com.boulangerie.abonnements.exception.CsvImportException;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.repository.LigneAbonnementRepository;
import com.boulangerie.abonnements.service.AbonnementConsommationImportService;
import com.boulangerie.abonnements.service.CsvImportService;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.swing.text.DateFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CsvImportServiceImpl implements CsvImportService {

    private static final String ABONNEMENT_ID = "abonnementId";
    private static final String CLIENT_ID = "clientId";
    private static final String DATE = "date";
    private static final String QUANTITE = "quantite";

//    private final static DateTimeFormatter CVS_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LigneAbonnementRepository ligneRepository;

    private final AbonnementConsommationImportService consommationImportService;

    @Override
    public ConsommationImportResultDto importerConsommationMensuelle(
            MultipartFile fichier,
            YearMonth periode
    ) {
        Objects.requireNonNull(
                fichier,
                "Le fichier CSV est obligatoire"
        );

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        if (fichier.isEmpty()) {
            throw new BadRequestException(
                    "Le fichier CSV est vide"
            );
        }

        ConstructionCommandesResult construction =
                construireCommandes(
                        fichier,
                        periode
                );

        List<ImportConsommationCommand> commandes =
                construction.commandes();

        int lignesIgnorees =
                construction.lignesIgnorees();

        if (commandes.isEmpty()) {

            return new ConsommationImportResultDto()
                    .setLignesTraitees(0)
                    .setConsommationsCreees(0)
                    .setConsommationsModifiees(0)
                    .setConsommationsInchangees(0)
                    .setLignesIgnorees(lignesIgnorees)
                    .setErreurs(List.of());
        }

        ImportConsommationStats stats =
                consommationImportService.importer(
                        commandes
                );

        return new ConsommationImportResultDto()
                .setLignesTraitees(
                        stats.getLignesTraitees()
                )
                .setConsommationsCreees(
                        stats.getConsommationsCreees()
                )
                .setConsommationsModifiees(
                        stats.getConsommationsModifiees()
                )
                .setConsommationsInchangees(
                        stats.getConsommationsInchangees()
                )
                .setLignesIgnorees(
                        lignesIgnorees
                )
                .setErreurs(List.of());
    }

    private ConstructionCommandesResult construireCommandes(
            MultipartFile fichier,
            YearMonth periode
    ) {

        try (
                InputStream inputStream =
                        fichier.getInputStream();

                BOMInputStream bomInputStream =
                        BOMInputStream.builder()
                                .setInputStream(inputStream)
                                .get();

                Reader reader =
                        new InputStreamReader(
                                bomInputStream,
                                StandardCharsets.UTF_8
                        );

                CSVParser parser = CSVFormat.DEFAULT.builder()
                                .setDelimiter(';')
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreEmptyLines(true)
                                .setTrim(true)
                                .build()
                                .parse(reader)
        ) {

            verifierEntete(parser);

            List<ImportConsommationCommand> commandes =
                    new ArrayList<>();

            int lignesIgnorees = 0;
            int numeroLigne = 1;

            for (CSVRecord record : parser) {

                numeroLigne++;

                if (recordIsEmpty(record)) {
                    lignesIgnorees++;
                    continue;
                }

                ImportConsommationCommand commande =
                        construireCommande(
                                record,
                                periode,
                                numeroLigne
                        );

                if (commande == null) {
                    lignesIgnorees++;
                    continue;
                }

                commandes.add(commande);
            }

            return new ConstructionCommandesResult(
                    commandes,
                    lignesIgnorees
            );

        } catch (IOException e) {

            throw new CsvImportException(
                    "Erreur lors de la lecture du fichier CSV",
                    e
            );
        }
    }

    private boolean recordIsEmpty(CSVRecord record) {
        return record.get(DATE) == null || record.get(QUANTITE) == null;
    }

    private void verifierEntete(CSVParser parser) {

        List<String> attendues = List.of(
                ABONNEMENT_ID,
                CLIENT_ID,
                DATE,
                QUANTITE
        );

        List<String> presentes =
                parser.getHeaderNames()
                        .stream()
                        .map(this::normaliserEntete)
                        .toList();

        if (!presentes.equals(attendues)) {

            throw new CsvImportException(
                    "En-tête CSV invalide. "
                            + "Colonnes attendues : "
                            + String.join(";", attendues)
                            + ". Colonnes trouvées : "
                            + String.join(";", presentes)
            );
        }
    }

    private String normaliserEntete(String header) {
        if (header == null) {
            return "";
        }
        return header.replace("\uFEFF", "").trim();
    }

    private ImportConsommationCommand construireCommande(
            CSVRecord record,
            YearMonth periode,
            int numeroLigne
    ) {
        Long abonnementId =
                lireLong(
                        record,
                        ABONNEMENT_ID,
                        numeroLigne
                );

        Long clientId =
                lireLong(
                        record,
                        CLIENT_ID,
                        numeroLigne
                );

        LocalDate date =
                lireDate(
                        record,
                        periode,
                        numeroLigne
                );

        BigDecimal quantite = lireQuantite(
                        record,
                        numeroLigne
                );

        LigneAbonnement ligne = ligneRepository
                        .findByAbonnementIdAndClientId(
                                abonnementId,
                                clientId
                        )
                        .orElseThrow(() ->
                                new CsvImportException(
                                        "Ligne introuvable à la ligne "
                                                + numeroLigne
                                                + " : abonnementId="
                                                + abonnementId
                                                + ", clientId="
                                                + clientId
                                )
                        );

        return new ImportConsommationCommand()
                .setLigneId(ligne.getId())
                .setDate(date)
                .setQuantite(quantite);
    }

    private Long lireLong(
            CSVRecord record,
            String colonne,
            int numeroLigne
    ) {
        String valeur = record.get(colonne);

        if (valeur == null || valeur.isBlank()) {

            throw new CsvImportException(
                    "Valeur manquante pour "
                            + colonne
                            + " à la ligne "
                            + numeroLigne
            );
        }

        try {
            return Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            throw new CsvImportException(
                    "Identifiant invalide pour "
                            + colonne
                            + " à la ligne "
                            + numeroLigne
                            + " : "
                            + valeur
            );
        }
    }

    private LocalDate lireDate(
            CSVRecord record,
            YearMonth periode,
            int numeroLigne
    ) {
        String valeur = record.get(DATE);

        if (valeur == null || valeur.isBlank()) {
            throw new CsvImportException("Date manquante à la ligne " + numeroLigne);
        }
        LocalDate date;
        try {
            date = LocalDate.parse(valeur.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            throw new CsvImportException(
                    "Date invalide à la ligne "
                            + numeroLigne
                            + " : "
                            + valeur
            );
        }

        if (!YearMonth.from(date).equals(periode)) {
            throw new CsvImportException(
                    "La date "
                            + date
                            + " n'appartient pas à la période "
                            + periode
                            + " à la ligne "
                            + numeroLigne
            );
        }
        return date;
    }

    private BigDecimal lireQuantite(
            CSVRecord record,
            int numeroLigne
    ) {
        String valeur = record.get(QUANTITE);

        if (valeur == null || valeur.isBlank()) {

            throw new CsvImportException(
                    "Quantité manquante à la ligne "
                            + numeroLigne
            );
        }

        try {
            BigDecimal quantite = new BigDecimal(valeur.trim());
            if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
                throw new CsvImportException(
                        "La quantité doit être "
                                + "strictement positive à la ligne "
                                + numeroLigne
                );
            }
            return quantite;
        } catch (NumberFormatException e) {
            throw new CsvImportException(
                    "Quantité invalide à la ligne "
                            + numeroLigne
                            + " : "
                            + valeur
            );
        }
    }

    private record ConstructionCommandesResult(
            List<ImportConsommationCommand> commandes,
            int lignesIgnorees
    ) {
    }
}