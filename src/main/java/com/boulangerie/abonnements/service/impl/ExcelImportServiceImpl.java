package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.dto.ConsommationDto;
import com.boulangerie.abonnements.dto.ConsommationImportResultDto;
import com.boulangerie.abonnements.dto.ImportConsommationCommand;
import com.boulangerie.abonnements.exception.ExcelImportException;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.repository.LigneAbonnementRepository;
import com.boulangerie.abonnements.service.AbonnementConsommationImportService;
import com.boulangerie.abonnements.service.AbonnementService;
import com.boulangerie.abonnements.service.ExcelImportService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

    private static final int CLIENT_COLUMN = 0;
    private static final int FIRST_DAY_COLUMN = 1;

    private static final int TOTAL_COLUMN = 32;
    private static final int PRIX_UNITAIRE_COLUMN = 33;
    private static final int MONTANT_MENSUEL_COLUMN = 34;
    private static final int MONTANT_PAYE_COLUMN = 35;
    private static final int RELIQUAT_COLUMN = 36;

    /**
     * Hidden technical columns.
     */
    private static final int ABONNEMENT_ID_COLUMN = 37;
    private static final int CLIENT_ID_COLUMN = 38;

    private static final String SHEET_NAME = "Feuil1";

    private final AbonnementService abonnementService;
    private final LigneAbonnementRepository ligneRepository;
    private final AbonnementConsommationImportService consommationImportService;

    @Override
    public ConsommationImportResultDto importerConsommationMensuelle(
            Long abonnementId,
            MultipartFile fichier,
            YearMonth periode
    ) {
        Objects.requireNonNull(
                abonnementId,
                "L'abonnement est obligatoire"
        );

        Objects.requireNonNull(
                fichier,
                "Le fichier Excel est obligatoire"
        );

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        if (fichier.isEmpty()) {
            throw new BadRequestException(
                    "Le fichier Excel est vide."
            );
        }

        try (
                InputStream inputStream = fichier.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {

            Sheet sheet = trouverFeuille(workbook);

            validerStructure(
                    sheet,
                    periode
            );

            List<ImportConsommationCommand> commandes =
                    construireCommandes(
                            sheet,
                            abonnementId,
                            periode
                    );

            int importees =
                    consommationImportService.importer(
                            commandes
                    );

            return new ConsommationImportResultDto()
                    .setConsommationsImportees(importees)
                    .setLignesTraitees(
                            compterLignesClients(commandes)
                    )
                    .setLignesIgnorees(0);

        } catch (IOException e) {

            throw new ExcelImportException(
                    "Erreur lors de la lecture du fichier Excel.",
                    e
            );

        }
    }

    @Override
    public ConsommationImportResultDto importerConsommationMensuelle(
            MultipartFile fichier,
            YearMonth periode
    ) {

        Objects.requireNonNull(
                fichier,
                "Le fichier Excel est obligatoire"
        );

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        if (fichier.isEmpty()) {
            throw new BadRequestException(
                    "Le fichier Excel est vide."
            );
        }

        try (
                InputStream inputStream = fichier.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {

            Sheet sheet = trouverFeuille(workbook);

            validerStructure(
                    sheet,
                    periode
            );

            /*
             * IMPORTANT :
             *
             * Ici, nous ne faisons aucune écriture DB.
             *
             * Nous construisons uniquement les commandes.
             */
            List<ImportConsommationCommand> commandes =
                    construireCommandes(
                            sheet,
                            periode
                    );

            if (commandes.isEmpty()) {

                return new ConsommationImportResultDto()
                        .setConsommationsImportees(0)
                        .setLignesTraitees(0)
                        .setLignesIgnorees(
                                sheet.getLastRowNum() + 1
                        );
            }

            /*
             * UN SEUL appel au service transactionnel.
             *
             * Si une seule consommation est invalide,
             * toute la transaction est rollbackée.
             */
            int importees = consommationImportService.importer(commandes);

            return new ConsommationImportResultDto()
                    .setConsommationsImportees(importees)
                    .setLignesTraitees(compterLignesClients(commandes))
                    .setLignesIgnorees(0);
        } catch (IOException e) {
            throw new ExcelImportException("Erreur lors de la lecture du fichier Excel.", e);
        }
    }

    private List<ImportConsommationCommand> construireCommandes(Sheet sheet, YearMonth periode) {
        List<ImportConsommationCommand> commandes = new ArrayList<>();
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);

            if (row == null || rowIsEmpty(row)) {
                continue;
            }

            if(!estLigneClient(row)){
                continue;
            }
            Long abonnementExcelId = lireIdCache(row, ABONNEMENT_ID_COLUMN);

            Long clientId = lireIdCache(row, CLIENT_ID_COLUMN);

            /*
             * Header, titre abonnement,
             * lignes de total, etc.
             */
            if (abonnementExcelId == null && clientId == null) {
                continue;
            }

            /*
             * Les deux IDs doivent toujours être présents.
             */
            if (abonnementExcelId == null || clientId == null) {

                throw new ExcelImportException(
                        "Ligne "
                                + (rowIndex + 1)
                                + " : abonnementId et clientId "
                                + "sont obligatoires."
                );
            }

            LigneAbonnement ligne = trouverLigne(abonnementExcelId, clientId);

            /*
             * Une ligne Excel = un client.
             */
            List<ImportConsommationCommand> lignes =
                    construireCommandesLigne(
                            row,
                            ligne,
                            abonnementExcelId,
                            clientId,
                            periode
                    );

            commandes.addAll(lignes);
        }

        return commandes;
    }

    private List<ImportConsommationCommand> construireCommandes(
            Sheet sheet,
            Long abonnementId,
            YearMonth periode
    ) {

        List<ImportConsommationCommand> commandes =
                new ArrayList<>();

        for (
                int rowIndex = 0;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++
        ) {

            Row row = sheet.getRow(rowIndex);

            if (row == null || rowIsEmpty(row)) {
                continue;
            }

            Long abonnementExcelId =
                    lireIdCache(
                            row,
                            ABONNEMENT_ID_COLUMN
                    );

            Long clientId =
                    lireIdCache(
                            row,
                            CLIENT_ID_COLUMN
                    );

            // Header / titre / total
            if (abonnementExcelId == null && clientId == null) {
                continue;
            }

            if (abonnementExcelId == null || clientId == null) {
                throw new ExcelImportException(
                        "Ligne " + (rowIndex + 1)
                                + " : abonnementId et clientId "
                                + "sont obligatoires."
                );
            }

            /*
             * Protection contre l'utilisation d'un fichier
             * appartenant à un autre abonnement.
             */
            if (!Objects.equals(
                    abonnementId,
                    abonnementExcelId
            )) {

                throw new ExcelImportException(
                        "Ligne " + (rowIndex + 1)
                                + " : l'abonnement du fichier ("
                                + abonnementExcelId
                                + ") ne correspond pas "
                                + "à l'abonnement demandé ("
                                + abonnementId
                                + ")."
                );
            }

            LigneAbonnement ligne =
                    trouverLigne(
                            abonnementId,
                            clientId
                    );

            commandes.addAll(
                    construireCommandesLigne(
                            row,
                            ligne,
                            abonnementId,
                            clientId,
                            periode
                    )
            );
        }
        return commandes;
    }

    private int compterLignesClients(
            List<ImportConsommationCommand> commandes
    ) {

        return (int) commandes.stream()
                .map(command ->
                        command.getAbonnementId()
                                + ":"
                                + command.getClientId()
                )
                .distinct()
                .count();
    }


    private List<ImportConsommationCommand> construireCommandesLigne(
            Row row,
            LigneAbonnement ligne,
            Long abonnementId,
            Long clientId,
            YearMonth periode
    ) {

        List<ImportConsommationCommand> commandes =
                new ArrayList<>();

        for (
                int jour = 1;
                jour <= periode.lengthOfMonth();
                jour++
        ) {

            int column =
                    FIRST_DAY_COLUMN + jour - 1;

            BigDecimal quantite =
                    lireBigDecimal(
                            row,
                            column
                    );

            /*
             * Cellule vide ou zéro :
             * aucune consommation à importer.
             */
            if (
                    quantite == null
                            || quantite.compareTo(
                            BigDecimal.ZERO
                    ) == 0
            ) {
                continue;
            }

            if (
                    quantite.compareTo(
                            BigDecimal.ZERO
                    ) < 0
            ) {

                throw new ExcelImportException(
                        "Quantité négative à la ligne "
                                + row.getRowNum()
                                + ", jour "
                                + jour
                );
            }

            LocalDate date =
                    periode.atDay(jour);

            commandes.add(
                    new ImportConsommationCommand()
                            .setAbonnementId(abonnementId)
                            .setClientId(clientId)
                            .setLigneId(ligne.getId())
                            .setDate(date)
                            .setQuantite(quantite)
            );
        }

        return commandes;
    }

    private LigneAbonnement trouverLigne(
            Long abonnementId,
            Long clientId
    ) {

        LigneAbonnement ligne =
                ligneRepository
                        .findByAbonnementIdAndClientId(
                                abonnementId,
                                clientId
                        )
                        .orElseThrow(() ->
                                new ExcelImportException(
                                        "Aucune ligne d'abonnement "
                                                + "trouvée pour "
                                                + "abonnementId="
                                                + abonnementId
                                                + " et clientId="
                                                + clientId
                                )
                        );

        /*
         * Defensive verification.
         */
        if (ligne.getAbonnement() == null
                || !Objects.equals(
                ligne.getAbonnement().getId(),
                abonnementId
        )) {

            throw new ExcelImportException(
                    "L'abonnement de la ligne "
                            + ligne.getId()
                            + " ne correspond pas à abonnementId="
                            + abonnementId
            );
        }

        if (ligne.getClient() == null
                || !Objects.equals(
                ligne.getClient().getId(),
                clientId
        )) {

            throw new ExcelImportException(
                    "Le client de la ligne "
                            + ligne.getId()
                            + " ne correspond pas à clientId="
                            + clientId
            );
        }

        return ligne;
    }

    /**
     * Vérification supplémentaire contre une incohérence
     * entre les IDs techniques du fichier et l'entité chargée.
     */
    private void verifierIdentite(
            LigneAbonnement ligne,
            Long abonnementId,
            Long clientId
    ) {

        if (ligne.getAbonnement() == null
                || !Objects.equals(
                ligne.getAbonnement().getId(),
                abonnementId
        )) {

            throw new BadRequestException(
                    "La ligne Excel référence un abonnement invalide."
            );
        }

        if (ligne.getClient() == null
                || !Objects.equals(
                ligne.getClient().getId(),
                clientId
        )) {

            throw new BadRequestException(
                    "La ligne Excel référence un client invalide."
            );
        }
    }

    private int importerLigne(
            Row row,
            LigneAbonnement ligne,
            YearMonth periode
    ) {
        int importees = 0;

        for (int jour = 1;
             jour <= periode.lengthOfMonth();
             jour++) {

            int column =
                    FIRST_DAY_COLUMN + jour - 1;

            BigDecimal quantite =
                    lireBigDecimal(row, column);

            if (quantite == null ||
                    quantite.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            LocalDate date =
                    periode.atDay(jour);

            ConsommationDto dto =
                    new ConsommationDto()
                            .setDate(date)
                            .setQuantite(quantite);

            abonnementService.synchroniserConsommation(
                    ligne.getId(),
                    dto
            );

            importees++;
        }

        return importees;
    }


    private Long lireIdCache(
            Row row,
            int column
    ) {

        Cell cell = row.getCell(column);

        if (cell == null) {
            throw new BadRequestException(
                    "Identifiant Excel manquant à la colonne "
                            + column
            );
        }

        if (cell.getCellType() != CellType.NUMERIC) {
            throw new BadRequestException(
                    "Identifiant Excel invalide : "
                            + cell.toString()
            );
        }

        return (long) cell.getNumericCellValue();
    }

    private BigDecimal lireBigDecimal(
            Row row,
            int column
    ) {

        Cell cell =
                row.getCell(column);

        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {

            case NUMERIC -> {

                double value =
                        cell.getNumericCellValue();

                if (!Double.isFinite(value)) {
                    throw new BadRequestException(
                            "Quantité Excel invalide."
                    );
                }

                yield BigDecimal.valueOf(value);
            }

            case STRING -> {

                String value =
                        cell.getStringCellValue()
                                .trim()
                                .replace(",", ".");

                if (value.isBlank()) {
                    yield null;
                }

                try {

                    yield new BigDecimal(value);

                } catch (NumberFormatException e) {

                    throw new ExcelImportException(
                            "Quantité Excel invalide : "
                                    + value,
                            e
                    );
                }
            }

            case FORMULA -> {

                if (
                        cell.getCachedFormulaResultType()
                                == CellType.NUMERIC
                ) {

                    double value =
                            cell.getNumericCellValue();

                    if (!Double.isFinite(value)) {
                        throw new BadRequestException(
                                "Quantité Excel invalide."
                        );
                    }

                    yield BigDecimal.valueOf(value);
                }

                yield null;
            }

            case BLANK -> null;

            default ->
                    throw new ExcelImportException(
                            "Type de cellule invalide "
                                    + "pour une quantité."
                    );
        };
    }

    private boolean rowIsEmpty(Row row) {

        if (row.getLastCellNum() < 0) {
            return true;
        }

        for (
                int column = 0;
                column < row.getLastCellNum();
                column++
        ) {

            Cell cell =
                    row.getCell(column);

            if (cell == null) {
                continue;
            }

            if (cell.getCellType()
                    == CellType.BLANK) {
                continue;
            }

            if (
                    cell.getCellType()
                            == CellType.STRING
                            && cell.getStringCellValue()
                            .isBlank()
            ) {
                continue;
            }

            return false;
        }

        return true;
    }

    private Sheet trouverFeuille(
            Workbook workbook
    ) {

        if (workbook.getNumberOfSheets() == 0) {

            throw new ExcelImportException(
                    "Le fichier Excel ne contient "
                            + "aucune feuille."
            );
        }

        Sheet sheet =
                workbook.getSheet(
                        SHEET_NAME
                );

        if (sheet == null) {

            throw new ExcelImportException(
                    "La feuille '"
                            + SHEET_NAME
                            + "' est introuvable."
            );
        }

        return sheet;
    }

    private boolean estLigneClient(Row row) {

        Cell abonnementIdCell =
                row.getCell(ABONNEMENT_ID_COLUMN);

        Cell clientIdCell =
                row.getCell(CLIENT_ID_COLUMN);

        return estCelluleNumerique(abonnementIdCell)
                && estCelluleNumerique(clientIdCell);
    }

    private boolean estCelluleNumerique(Cell cell) {

        if (cell == null) {
            return false;
        }

        return cell.getCellType() == CellType.NUMERIC
                && !DateUtil.isCellDateFormatted(cell);
    }

    private void validerStructure(
            Sheet sheet,
            YearMonth periode
    ) {

        Row firstRow =
                sheet.getRow(0);

        if (firstRow == null) {

            throw new ExcelImportException(
                    "Le fichier Excel est vide."
            );
        }

        /*
         * Dernière colonne correspondant
         * aux jours du mois.
         */
        int derniereColonneJour =
                FIRST_DAY_COLUMN
                        + periode.lengthOfMonth()
                        - 1;

        if (
                firstRow.getLastCellNum()
                        <= derniereColonneJour
        ) {

            throw new ExcelImportException(
                    "La structure du fichier Excel "
                            + "ne correspond pas au mois "
                            + periode + "."
            );
        }

        /*
         * Les colonnes techniques doivent
         * également être présentes.
         */
        if (
                firstRow.getLastCellNum()
                        <= CLIENT_ID_COLUMN
        ) {

            throw new ExcelImportException(
                    "Le fichier Excel ne contient pas "
                            + "les colonnes techniques "
                            + "abonnementId/clientId."
            );
        }
    }

    private String ligneNom(Row row) {

        Cell cell =
                row.getCell(CLIENT_COLUMN);

        if (
                cell == null
                        || cell.getCellType()
                        == CellType.BLANK
        ) {
            return "client inconnu";
        }

        if (
                cell.getCellType()
                        == CellType.STRING
        ) {
            return cell.getStringCellValue();
        }

        return "client inconnu";
    }

    private String construireMessageErreur(
            String client,
            RuntimeException exception
    ) {

        String message =
                exception.getMessage();

        if (
                message == null
                        || message.isBlank()
        ) {

            message =
                    exception
                            .getClass()
                            .getSimpleName();
        }

        return client
                + " : "
                + message;
    }
}