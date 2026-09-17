package com.boulangerie.abonnements.service.impl;

import com.boulangerie.abonnements.dto.ConsommationDto;
import com.boulangerie.abonnements.dto.ConsommationImportResultDto;
import com.boulangerie.abonnements.dto.ImportConsommationCommand;
import com.boulangerie.abonnements.dto.ImportConsommationStats;
import com.boulangerie.abonnements.exception.ExcelImportException;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.repository.LigneAbonnementRepository;
import com.boulangerie.abonnements.service.AbonnementConsommationImportService;
import com.boulangerie.abonnements.service.AbonnementService;
import com.boulangerie.abonnements.service.ExcelImportService;
import com.boulangerie.reporting.service.impl.ExcelExportServiceImpl;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
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

    private static final int FIRST_DAY_COLUMN = 1;
    private static final String DETAILS_SHEET = "Détails";

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

            ExcelLayout layout = ExcelLayout.forPeriod(periode);
            Sheet sheet = trouverFeuilleDetails(workbook);

            validerStructure(
                    sheet,
                    layout,
                    periode
            );

            List<ImportConsommationCommand> commandes =
                    construireCommandes(
                            sheet,
                            abonnementId,
                            periode
                    );

            ImportConsommationStats consommationStats = consommationImportService.importer(commandes);

            return new ConsommationImportResultDto()
                    .setLignesTraitees(
                            consommationStats.getLignesTraitees()
                    )
                    .setConsommationsCreees(
                            consommationStats.getConsommationsCreees()
                    )
                    .setConsommationsModifiees(
                            consommationStats.getConsommationsModifiees()
                    )
                    .setConsommationsInchangees(
                            consommationStats.getConsommationsInchangees()
                    )
                    .setLignesIgnorees(0)
                    .setErreurs(List.of());

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

            ExcelLayout layout = ExcelLayout.forPeriod(periode);
            Sheet sheet = trouverFeuilleDetails(workbook);

            validerStructure(
                    sheet,
                    layout,
                    periode
            );

            /*
             * IMPORTANT :
             *
             * Ici, nous ne faisons aucune écriture DB.
             *
             * Nous construisons uniquement les commandes.
             */

            ConstructionCommandesResult construction = construireCommandes(
                    sheet, periode
            );
            List<ImportConsommationCommand> commandes = construction.commandes();
            /*
             * UN SEUL appel au service transactionnel.
             *
             * Si une seule consommation est invalide,
             * toute la transaction est rollbackée.
             */
            int lignesIgnorees = construction.lignesIgnorees();


            if (commandes.isEmpty()) {
                return new ConsommationImportResultDto()
                        .setLignesTraitees(0)
                        .setConsommationsCreees(0)
                        .setConsommationsModifiees(0)
                        .setConsommationsInchangees(0)
                        .setLignesIgnorees(lignesIgnorees)
                        .setErreurs(List.of());
            }


            ImportConsommationStats consommationStats = consommationImportService.importer(commandes);

            return new ConsommationImportResultDto()
                    .setLignesTraitees(
                            consommationStats.getLignesTraitees()
                    )
                    .setConsommationsCreees(
                            consommationStats.getConsommationsCreees()
                    )
                    .setConsommationsModifiees(
                            consommationStats.getConsommationsModifiees()
                    )
                    .setConsommationsInchangees(
                            consommationStats.getConsommationsInchangees()
                    )
                    .setLignesIgnorees(lignesIgnorees)
                    .setErreurs(List.of());
        } catch (IOException e) {
            throw new ExcelImportException("Erreur lors de la lecture du fichier Excel.", e);
        }
    }

    private ConstructionCommandesResult construireCommandes(
            Sheet sheet,
            YearMonth periode
    ) {
        List<ImportConsommationCommand> commandes =
                new ArrayList<>();

        int lignesIgnorees = 0;

        ExcelLayout layout =
                ExcelLayout.forPeriod(periode);

        for (
                int rowIndex = 0;
                rowIndex <= sheet.getLastRowNum();
                rowIndex++
        ) {

            Row row = sheet.getRow(rowIndex);

            if (row == null || rowIsEmpty(row)) {
                continue;
            }

            /*
             * Header, titre abonnement, lignes de total,
             * etc.
             */
            if (!estLigneClient(row, layout)) {
                continue;
            }

            Long abonnementExcelId =
                    lireIdCache(
                            row,
                            layout.abonnementIdColumn()
                    );

            Long clientId =
                    lireIdCache(
                            row,
                            layout.clientIdColumn()
                    );

            /*
             * Ligne reconnue comme ligne client,
             * mais sans aucun identifiant.
             *
             * On l'ignore.
             */
            if (abonnementExcelId == null && clientId == null) {
                lignesIgnorees++;
                continue;
            }

            /*
             * Un seul des deux IDs est présent :
             * fichier incohérent.
             *
             * Ce n'est PAS une ligne ignorée.
             * L'import doit échouer.
             */
            if (
                    abonnementExcelId == null
                            || clientId == null
            ) {

                throw new ExcelImportException(
                        "Ligne "
                                + (rowIndex + 1)
                                + " : abonnementId et clientId "
                                + "sont obligatoires."
                );
            }

            LigneAbonnement ligne =
                    trouverLigne(
                            abonnementExcelId,
                            clientId
                    );

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

            /*
             * La ligne client existe mais ne contient
             * aucune consommation importable.
             */
            if (lignes.isEmpty()) {
                lignesIgnorees++;
                continue;
            }

            commandes.addAll(lignes);
        }

        return new ConstructionCommandesResult(
                commandes,
                lignesIgnorees
        );
    }

    private List<ImportConsommationCommand> construireCommandes(
            Sheet sheet,
            Long abonnementId,
            YearMonth periode
    ) {

        List<ImportConsommationCommand> commandes = new ArrayList<>();

        ExcelLayout layout = ExcelLayout.forPeriod(periode);
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum();              rowIndex++) {

            Row row = sheet.getRow(rowIndex);
            if (row == null || rowIsEmpty(row)) {
                continue;
            }

            if (!estLigneClient(row, layout)) {
                continue;
            }

            Long abonnementExcelId = lireIdCache(row, layout.abonnementIdColumn());

            Long clientId = lireIdCache(row, layout.clientIdColumn());


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

            LigneAbonnement ligne = trouverLigne(abonnementId, clientId);

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

        List<ImportConsommationCommand> commandes = new ArrayList<>();

        ExcelLayout layout = ExcelLayout.forPeriod(periode);

        for (int jour = 1; jour <= layout.nombreJours; jour++) {
            int column = layout.dayColumn(jour);
            BigDecimal quantite = lireQuantite(row, column);
            /*
             * Cellule vide ou zéro :
             * aucune consommation à importer.
             */
            if (quantite == null || quantite.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            if (quantite.compareTo(BigDecimal.ZERO) < 0
            ) {
                throw new ExcelImportException("Quantité négative à la ligne "
                                + row.getRowNum()
                                + ", jour "
                                + jour
                );
            }

            LocalDate date = periode.atDay(jour);

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

    private BigDecimal lireQuantite(
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

    private Sheet trouverFeuilleDetails(
            Workbook workbook
    ) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new ExcelImportException(
                    "Le fichier Excel ne contient aucune feuille."
            );
        }

        Sheet sheet = workbook.getSheet(DETAILS_SHEET);

        if (sheet == null) {
            throw new ExcelImportException(
                    "La feuille '" +
                            DETAILS_SHEET +
                            "' est introuvable."
            );
        }

        return sheet;
    }

    private boolean estLigneClient(
            Row row,
            ExcelLayout layout
    ) {
        Cell abonnementIdCell =
                row.getCell(
                        layout.abonnementIdColumn()
                );

        Cell clientIdCell =
                row.getCell(
                        layout.clientIdColumn()
                );

        return estCelluleNumerique(abonnementIdCell)
                && estCelluleNumerique(clientIdCell);
    }

    private boolean estCelluleNumerique(Cell cell) {

        return cell != null && cell.getCellType() == CellType.NUMERIC;
    }

    private void validerStructure(
            Sheet sheet,
            ExcelLayout layout,
            YearMonth periode
    ) {
        if (sheet.getLastRowNum() < 0) {
            throw new ExcelImportException(
                    "La feuille Détails est vide."
            );
        }

        int lastDayColumn = FIRST_DAY_COLUMN
                        + periode.lengthOfMonth()
                        - 1;

        Row headerRow =
                sheet.getRow(0);

        if (headerRow == null) {
            throw new ExcelImportException(
                    "L'en-tête de la feuille Détails est absent."
            );
        }

        if (headerRow.getLastCellNum()
                <= lastDayColumn) {

            throw new ExcelImportException(
                    "La structure du fichier Excel "
                            + "ne correspond pas au mois "
                            + periode
                            + "."
            );
        }

        if (headerRow.getLastCellNum() <= layout.clientColumn()) {
            throw new ExcelImportException(
                    "Le fichier Excel ne contient pas "
                            + "les colonnes techniques "
                            + "abonnementId/clientId."
            );
        }
    }
    private record ExcelLayout(
            int clientColumn,
            int firstDayColumn,
            int totalColumn,
            int prixUnitaireColumn,
            int montantMensuelColumn,
            int montantPayeColumn,
            int reliquatColumn,
            int abonnementIdColumn,
            int clientIdColumn,
            int nombreJours
    ) {

        static ExcelLayout forPeriod(
                YearMonth periode
        ) {
            int firstDayColumn = 1;

            int totalColumn =
                    firstDayColumn
                            + periode.lengthOfMonth();

            return new ExcelLayout(
                    0,
                    firstDayColumn,
                    totalColumn,
                    totalColumn + 1,
                    totalColumn + 2,
                    totalColumn + 3,
                    totalColumn + 4,
                    totalColumn + 5,
                    totalColumn + 6,
                    periode.lengthOfMonth()
            );
        }

        public int dayColumn(int jour) {
            if(jour < 1 || jour > nombreJours){
                throw new IllegalArgumentException("Jour invalide : " + jour);
            }
            return firstDayColumn + jour - 1;
        }
    }

    private record ConstructionCommandesResult(
            List<ImportConsommationCommand> commandes,
            int lignesIgnorees
    ) {
    }
}