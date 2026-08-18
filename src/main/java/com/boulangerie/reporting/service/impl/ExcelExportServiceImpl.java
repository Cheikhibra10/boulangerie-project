package com.boulangerie.reporting.service.impl;

import com.boulangerie.abonnements.api.AbonnementConsommationReportDto;
import com.boulangerie.abonnements.api.ConsommationMensuelleLigneDto;
import com.boulangerie.abonnements.api.ConsommationMensuelleReportDto;
import com.boulangerie.reporting.service.ExcelExportService;
import com.boulangerie.reporting.exception.ExcelExportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final int CLIENT_COLUMN = 0;

    private static final int FIRST_DAY_COLUMN = 1;

    /**
     * 31 possible days:
     *
     * 1 -> column 1
     * ...
     * 31 -> column 31
     */
    private static final int LAST_DAY_COLUMN = 31;

    private static final int TOTAL_COLUMN = 32;

    private static final int PRIX_UNITAIRE_COLUMN = 33;

    private static final int MONTANT_MENSUEL_COLUMN = 34;

    private static final int MONTANT_PAYE_COLUMN = 35;

    private static final int RELIQUAT_COLUMN = 36;

    private static final int ABONNEMENT_ID_COLUMN = 37;
    private static final int CLIENT_ID_COLUMN = 38;

    private static final int LAST_COLUMN = CLIENT_ID_COLUMN;

    private static final String SHEET_NAME = "Feuil1";

    private static final String TOTAL_QUANTITE_LABEL = "Quantité T";

    private static final String TOTAL_MENSUEL_LABEL = "Montant mensuel";

    private static final String TOTAL_GLOBAL_LABEL = "Montant globale";

    private static final String TOTAL_PAYE_LABEL = "Somme versée";

    private static final String TOTAL_RELIQUAT_LABEL = "Reliquat";

    @Override
    public byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    ) {

        Objects.requireNonNull(
                report,
                "Le rapport mensuel est obligatoire"
        );

        Objects.requireNonNull(
                report.getPeriode(),
                "La période du rapport est obligatoire"
        );

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            Styles styles = createStyles(workbook);

            /*
             * Hauteur par défaut des lignes.
             */
            sheet.setDefaultRowHeightInPoints(20);

            int currentRow = 0;

            /*
             * Une section Excel par abonnement.
             */
            for (AbonnementConsommationReportDto abonnement
                    : report.getAbonnements()) {

                currentRow = écrireAbonnement(
                        sheet,
                        currentRow,
                        report.getPeriode(),
                        abonnement,
                        styles
                );

                /*
                 * Espace visuel entre deux abonnements.
                 */
                currentRow += 2;
            }

            /*
             * Résumé global de tous les abonnements.
             */
            currentRow += 1;

            écrireTotalGlobal(
                    sheet,
                    currentRow,
                    report,
                    styles
            );

            /*
             * Garder la colonne "Nom" et les lignes d'en-tête visibles.
             */
            sheet.createFreezePane(
                    1,
                    2
            );

            /*
             * Largeurs adaptées au nombre de jours du mois.
             */
            appliquerLargeurs(
                    sheet,
                    report.getPeriode()
            );

            /*
             * Mise en page générale.
             */
            sheet.setPrintGridlines(false);

            return toBytes(workbook);

        } catch (IOException e) {

            throw new ExcelExportException(
                    "Erreur lors de la génération du fichier Excel",
                    e
            );
        }
    }

    /**
     * Écrit une section complète pour un abonnement.
     */
    private int écrireAbonnement(
            Sheet sheet,
            int rowIndex,
            YearMonth periode,
            AbonnementConsommationReportDto abonnement,
            Styles styles
    ) {

        /*
         * Header des dates.
         */
        Row headerRow = sheet.createRow(rowIndex);

        Row daysRow = sheet.createRow(rowIndex + 1);

        écrireEntete(
                headerRow,
                daysRow,
                periode,
                styles
        );

        int currentRow = rowIndex + 2;

        /*
         * Titre de l'abonnement.
         */
        Row abonnementRow = sheet.createRow(currentRow++);
        abonnementRow.setHeightInPoints(25);

        Cell abonnementCell = abonnementRow.createCell(CLIENT_COLUMN);

        abonnementCell.setCellValue(
                "Abonnement " +
                        abonnement.getAbonnementNom()
        );

        Cell abonnementIdCell =
                abonnementRow.createCell(ABONNEMENT_ID_COLUMN);

        abonnementIdCell.setCellValue(
                abonnement.getAbonnementId()
        );
        abonnementCell.setCellStyle(
                styles.abonnementStyle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        abonnementRow.getRowNum(),
                        abonnementRow.getRowNum(),
                        CLIENT_COLUMN,
                        LAST_COLUMN
                )
        );

        /*
         * Ligne "Nom".
         */
        Row nomRow =
                sheet.createRow(currentRow++);

        Cell nomCell =
                nomRow.createCell(CLIENT_COLUMN);

        nomCell.setCellValue("Nom");

        nomCell.setCellStyle(
                styles.headerStyle()
        );

        /*
         * Clients.
         */
        currentRow = écrireLignesClients(
                sheet,
                currentRow,
                abonnement.getAbonnementId(),
                abonnement.getLignes(),
                styles
        );

        /*
         * Résumé de cet abonnement.
         */
        currentRow = écrireTotalAbonnement(
                sheet,
                currentRow,
                abonnement,
                styles
        );

        return currentRow;
    }

    /**
     * Header :
     *
     * Date | 01/08/2026 | 02/08/2026 | ...
     *
     * puis :
     *
     *      | Vendredi   | Samedi     | ...
     */
    private void écrireEntete(
            Row headerRow,
            Row daysRow,
            YearMonth periode,
            Styles styles
    ) {

        écrireCelluleEntete(
                headerRow,
                CLIENT_COLUMN,
                "Date",
                styles
        );

        for (
                int jour = 1;
                jour <= periode.lengthOfMonth();
                jour++
        ) {

            LocalDate date =
                    periode.atDay(jour);

            int column =
                    FIRST_DAY_COLUMN + jour - 1;

            Cell dateCell =
                    headerRow.createCell(column);

            Cell clientIdHeader =
                    headerRow.createCell(CLIENT_ID_COLUMN);

            clientIdHeader.setCellValue("Client ID");
            clientIdHeader.setCellStyle(
                    styles.headerStyle()
            );


            dateCell.setCellValue(
                    DateTimeFormatter
                            .ofPattern("dd/MM/yyyy")
                            .format(date)
            );

            dateCell.setCellStyle(
                    styles.headerStyle()
            );

            Cell dayCell =
                    daysRow.createCell(column);

            dayCell.setCellValue(
                    capitalize(
                            date.getDayOfWeek()
                                    .getDisplayName(
                                            TextStyle.FULL,
                                            Locale.FRENCH
                                    )
                    )
            );

            dayCell.setCellStyle(
                    styles.subHeaderStyle()
            );
        }

        créerCelluleEntete(
                headerRow,
                TOTAL_COLUMN,
                "Total",
                styles
        );

        créerCelluleEntete(
                headerRow,
                PRIX_UNITAIRE_COLUMN,
                "P.unitaire",
                styles
        );

        créerCelluleEntete(
                headerRow,
                MONTANT_MENSUEL_COLUMN,
                "Montant mensuel",
                styles
        );

        créerCelluleEntete(
                headerRow,
                MONTANT_PAYE_COLUMN,
                "Somme versée",
                styles
        );

        créerCelluleEntete(
                headerRow,
                RELIQUAT_COLUMN,
                "Reliquat",
                styles
        );
        créerCelluleEntete(
                headerRow,
                ABONNEMENT_ID_COLUMN,
                "Abonnement ID",
                styles
        );

        créerCelluleEntete(
                headerRow,
                CLIENT_ID_COLUMN,
                "Client ID",
                styles
        );
    }

    private void créerCelluleEntete(
            Row row,
            int column,
            String value,
            Styles styles
    ) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(styles.headerStyle());
    }
    /**
     * Écrit toutes les lignes clients.
     */
    private int écrireLignesClients(
            Sheet sheet,
            int rowIndex,
            Long abonnementId,
            List<ConsommationMensuelleLigneDto> lignes,
            Styles styles
    ) {

        int currentRow = rowIndex;

        for (ConsommationMensuelleLigneDto ligne : lignes) {

            Row row = sheet.createRow(currentRow++);

            /*
             * ============================
             * NOM CLIENT
             * ============================
             */
            Cell nomCell = row.createCell(CLIENT_COLUMN);

            nomCell.setCellValue(construireNomClient(ligne));

            nomCell.setCellStyle(styles.clientStyle());

            /*
             * ============================
             * CONSOMMATIONS JOURNALIÈRES
             * ============================
             */
            for (Map.Entry<Integer, BigDecimal> entry : ligne.getConsommations().entrySet()) {

                int jour = entry.getKey();

                Cell cell = row.createCell(FIRST_DAY_COLUMN + jour - 1);

                BigDecimal quantite = entry.getValue();

                if (quantite != null && quantite.compareTo(BigDecimal.ZERO) != 0) {
                    cell.setCellValue(quantite.doubleValue());
                }
                cell.setCellStyle(styles.numberStyle());
            }

            /*
             * ============================
             * COLONNES CALCULÉES
             * ============================
             */
            écrireNombre(
                    row,
                    TOTAL_COLUMN,
                    ligne.getQuantiteTotale(),
                    styles.numberStyle()
            );

            écrireNombre(
                    row,
                    PRIX_UNITAIRE_COLUMN,
                    ligne.getPrixUnitaire(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    MONTANT_MENSUEL_COLUMN,
                    ligne.getMontant(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    MONTANT_PAYE_COLUMN,
                    ligne.getMontantPaye(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    RELIQUAT_COLUMN,
                    ligne.getReliquat(),
                    styles.currencyStyle()
            );

            /*
             * ============================
             * IDENTITÉ TECHNIQUE
             * ============================
             */

            Cell abonnementIdCell =
                    row.createCell(ABONNEMENT_ID_COLUMN);

            abonnementIdCell.setCellValue(
                    abonnementId
            );

            Cell clientIdCell =
                    row.createCell(CLIENT_ID_COLUMN);

            clientIdCell.setCellValue(
                    ligne.getClientId()
            );

        }

        return currentRow;
    }

    /**
     * Écrit les consommations jour par jour.
     */
    private void écrireConsommations(
            Row row,
            ConsommationMensuelleLigneDto ligne,
            Styles styles
    ) {

        if (ligne.getConsommations() == null) {
            return;
        }

        for (
                Map.Entry<Integer, BigDecimal> entry
                : ligne.getConsommations().entrySet()
        ) {

            Integer jour = entry.getKey();

            if (
                    jour == null
                            || jour < 1
                            || jour > 31
            ) {
                continue;
            }

            BigDecimal quantite =
                    entry.getValue();

            if (quantite == null) {
                continue;
            }

            int column =
                    FIRST_DAY_COLUMN + jour - 1;

            Cell cell =
                    row.createCell(column);

            /*
             * On laisse les consommations nulles
             * visuellement vides.
             */
            if (
                    quantite.compareTo(
                            BigDecimal.ZERO
                    ) != 0
            ) {

                cell.setCellValue(
                        quantite.doubleValue()
                );
            }

            cell.setCellStyle(
                    styles.numberStyle()
            );
        }
    }

    /**
     * Résumé d'un abonnement.
     *
     * Exemple :
     *
     * Quantité T       450
     * Montant mensuel  67 500
     * Montant globale  67 500
     * Somme versée     55 000
     * Reliquat         12 500
     */
    private int écrireTotalAbonnement(
            Sheet sheet,
            int rowIndex,
            AbonnementConsommationReportDto abonnement,
            Styles styles
    ) {
        int currentRow = rowIndex;

        /*
         * Quantité T
         */
        currentRow = écrireLigneTotal(
                sheet,
                currentRow,
                TOTAL_QUANTITE_LABEL,
                abonnement.getQuantiteTotale(),
                styles.numberStyle(),
                styles.totalLabelStyle()
        );

        /*
         * Montant mensuel
         */
        currentRow = écrireLigneTotal(
                sheet,
                currentRow,
                TOTAL_MENSUEL_LABEL,
                abonnement.getMontantMensuelTotal(),
                styles.currencyStyle(),
                styles.totalLabelStyle()
        );

        /*
         * Somme versée
         */
        currentRow = écrireLigneTotal(
                sheet,
                currentRow,
                TOTAL_PAYE_LABEL,
                abonnement.getMontantVerseTotal(),
                styles.currencyStyle(),
                styles.totalLabelStyle()
        );

        /*
         * Reliquat
         */
        currentRow = écrireLigneTotal(
                sheet,
                currentRow,
                TOTAL_RELIQUAT_LABEL,
                abonnement.getReliquatTotal(),
                styles.currencyStyle(),
                styles.totalLabelStyle()
        );

        return currentRow;
    }

    /**
     * Résumé global de tout le rapport.
     */
    private int écrireTotalGlobal(
            Sheet sheet,
            int rowIndex,
            ConsommationMensuelleReportDto report,
            Styles styles
    ) {

        int currentRow = rowIndex + 1;

        /*
         * ============================================================
         * TITRE
         * ============================================================
         */

        Row titleRow = sheet.createRow(currentRow++);
        titleRow.setHeightInPoints(28);
        Cell titleCell = titleRow.createCell(CLIENT_COLUMN);

        titleCell.setCellValue(
                "TOTAL GLOBAL - " + report.getPeriode()
        );

        titleCell.setCellStyle(
                styles.globalTotalTitleStyle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        titleRow.getRowNum(),
                        titleRow.getRowNum(),
                        CLIENT_COLUMN,
                        LAST_COLUMN
                )
        );

        /*
         * ============================================================
         * LABELS
         * ============================================================
         */

        Row labelRow = sheet.createRow(currentRow);

        créerCelluleTotalGlobal(
                labelRow,
                CLIENT_COLUMN,
                TOTAL_QUANTITE_LABEL,
                styles.globalTotalLabelStyle()
        );

        créerCelluleTotalGlobal(
                labelRow,
                CLIENT_COLUMN + 1,
                TOTAL_MENSUEL_LABEL,
                styles.globalTotalLabelStyle()
        );

        créerCelluleTotalGlobal(
                labelRow,
                CLIENT_COLUMN + 2,
                TOTAL_GLOBAL_LABEL,
                styles.globalTotalLabelStyle()
        );

        créerCelluleTotalGlobal(
                labelRow,
                CLIENT_COLUMN + 3,
                TOTAL_RELIQUAT_LABEL,
                styles.globalTotalLabelStyle()
        );

        /*
         * ============================================================
         * VALEURS
         * ============================================================
         */

        Row valueRow = sheet.createRow(++currentRow);

        écrireCelluleTotalGlobal(
                valueRow,
                CLIENT_COLUMN,
                report.getQuantiteTotale(),
                styles.globalTotalValueStyle()
        );

        écrireCelluleTotalGlobal(
                valueRow,
                CLIENT_COLUMN + 1,
                report.getMontantMensuelTotal(),
                styles.globalTotalValueStyle()
        );

        écrireCelluleTotalGlobal(
                valueRow,
                CLIENT_COLUMN + 2,
                report.getMontantVerseTotal(),
                styles.globalTotalValueStyle()
        );

        écrireCelluleTotalGlobal(
                valueRow,
                CLIENT_COLUMN + 3,
                report.getReliquatTotal(),
                styles.globalTotalValueStyle()
        );

        return currentRow + 1;
    }
    private void créerCelluleTotalGlobal(
            Row row,
            int column,
            String value,
            CellStyle style
    ) {
        Cell cell = row.createCell(column);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void écrireCelluleTotalGlobal(
            Row row,
            int column,
            BigDecimal value,
            CellStyle style
    ) {
        Cell cell = row.createCell(column);

        cell.setCellValue(
                value == null
                        ? 0D
                        : value.doubleValue()
        );

        cell.setCellStyle(style);
    }
    /**
     * Écrit une ligne :
     *
     * Quantité T       | valeur
     */
    private int écrireLigneTotal(
            Sheet sheet,
            int rowIndex,
            String label,
            BigDecimal value,
            CellStyle valueStyle,
            CellStyle labelStyle
    ) {
        Row row = sheet.createRow(rowIndex);

        /*
         * Libellé du total.
         */
        Cell labelCell = row.createCell(CLIENT_COLUMN);

        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        /*
         * Colonnes intermédiaires.
         *
         * Elles sont volontairement vides mais stylées
         * afin de conserver la structure visuelle
         * du tableau.
         */
        for (
                int column = CLIENT_COLUMN + 1;
                column < TOTAL_COLUMN;
                column++
        ) {
            Cell emptyCell = row.createCell(column);
            emptyCell.setCellStyle(labelStyle);
        }

        /*
         * Valeur du total.
         *
         * Toutes les valeurs de synthèse sont placées
         * dans la colonne "Total".
         */
        Cell valueCell = row.createCell(TOTAL_COLUMN);

        valueCell.setCellValue(
                value != null
                        ? value.doubleValue()
                        : 0D
        );

        valueCell.setCellStyle(valueStyle);

        return rowIndex + 1;
    }

    private void écrireNombre(
            Row row,
            int column,
            BigDecimal value,
            CellStyle style
    ) {

        Cell cell =
                row.createCell(column);

        if (value != null) {
            cell.setCellValue(
                    value.doubleValue()
            );
        } else {
            cell.setCellValue(0D);
        }

        cell.setCellStyle(style);
    }

    private void écrireCelluleEntete(
            Row row,
            int column,
            String value,
            Styles styles
    ) {

        Cell cell =
                row.createCell(column);

        cell.setCellValue(value);

        cell.setCellStyle(
                styles.headerStyle()
        );
    }



    private String construireNomClient(
            ConsommationMensuelleLigneDto ligne
    ) {

        String prenom =
                ligne.getPrenom();

        String nom =
                ligne.getNom();

        if (
                prenom == null
                        || prenom.isBlank()
        ) {
            return nom == null ? "" : nom;
        }

        if (
                nom == null
                        || nom.isBlank()
        ) {
            return prenom;
        }

        return prenom + " " + nom;
    }

    private String capitalize(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return value;
        }

        return value.substring(0, 1)
                .toUpperCase(Locale.FRENCH)
                + value.substring(1);
    }

    private byte[] toBytes(
            Workbook workbook
    ) throws IOException {

        try (
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            workbook.write(output);

            return output.toByteArray();
        }
    }

    private void appliquerLargeurs(
            Sheet sheet,
            YearMonth periode
    ) {
        sheet.setColumnWidth(
                CLIENT_COLUMN,
                30 * 256
        );

        for (int jour = 1; jour <= periode.lengthOfMonth(); jour++) {
            sheet.setColumnWidth(
                    FIRST_DAY_COLUMN + jour - 1,
                    13 * 256
            );
        }

        sheet.setColumnWidth(TOTAL_COLUMN, 14 * 256);
        sheet.setColumnWidth(PRIX_UNITAIRE_COLUMN, 14 * 256);
        sheet.setColumnWidth(MONTANT_MENSUEL_COLUMN, 18 * 256);
        sheet.setColumnWidth(MONTANT_PAYE_COLUMN, 16 * 256);
        sheet.setColumnWidth(RELIQUAT_COLUMN, 16 * 256);
        sheet.setColumnWidth(ABONNEMENT_ID_COLUMN, 15 * 256);
        sheet.setColumnWidth(CLIENT_ID_COLUMN, 15 * 256);
        sheet.setColumnHidden(ABONNEMENT_ID_COLUMN, true);
        sheet.setColumnHidden(CLIENT_ID_COLUMN, true);
    }

    private Styles createStyles(Workbook workbook) {

        /*
         * ============================================================
         * FONTS
         * ============================================================
         */

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);

        Font abonnementFont = workbook.createFont();
        abonnementFont.setBold(true);
        abonnementFont.setFontHeightInPoints((short) 14);

        Font totalFont = workbook.createFont();
        totalFont.setBold(true);

        Font globalTitleFont = workbook.createFont();
        globalTitleFont.setBold(true);
        globalTitleFont.setFontHeightInPoints((short) 13);

        Font globalLabelFont = workbook.createFont();
        globalLabelFont.setBold(true);


        /*
         * ============================================================
         * HEADER PRINCIPAL
         * ============================================================
         */

        CellStyle headerStyle = workbook.createCellStyle();

        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);

        headerStyle.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        headerStyle.setFont(headerFont);

        setThinBorders(headerStyle);

        /*
         * ============================================================
         * SOUS-HEADER : jours de la semaine
         * ============================================================
         */

        CellStyle subHeaderStyle = workbook.createCellStyle();

        subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        subHeaderStyle.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );
        subHeaderStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setThinBorders(subHeaderStyle);


        /*
         * ============================================================
         * TITRE ABONNEMENT
         * ============================================================
         */

        CellStyle abonnementStyle = workbook.createCellStyle();

        abonnementStyle.setFont(abonnementFont);
        abonnementStyle.setAlignment(
                HorizontalAlignment.LEFT
        );
        abonnementStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        abonnementStyle.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
        );
        abonnementStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setThinBorders(abonnementStyle);


        /*
         * ============================================================
         * CLIENT
         * ============================================================
         */

        CellStyle clientStyle = workbook.createCellStyle();

        clientStyle.setAlignment(
                HorizontalAlignment.LEFT
        );
        clientStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        setThinBorders(clientStyle);


        /*
         * ============================================================
         * NOMBRES
         * ============================================================
         */

        CellStyle numberStyle = workbook.createCellStyle();

        numberStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );
        numberStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        numberStyle.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.##")
        );

        setThinBorders(numberStyle);


        /*
         * ============================================================
         * MONNAIE
         * ============================================================
         */

        CellStyle currencyStyle = workbook.createCellStyle();

        currencyStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );
        currencyStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        currencyStyle.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0")
        );

        setThinBorders(currencyStyle);


        /*
         * ============================================================
         * TOTAL ABONNEMENT
         * ============================================================
         */

        CellStyle totalLabelStyle = workbook.createCellStyle();

        totalLabelStyle.setFont(totalFont);
        totalLabelStyle.setAlignment(
                HorizontalAlignment.LEFT
        );
        totalLabelStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        totalLabelStyle.setFillForegroundColor(
                IndexedColors.LIGHT_YELLOW.getIndex()
        );
        totalLabelStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setThinBorders(totalLabelStyle);


        /*
         * Valeurs des totaux abonnement
         */

        CellStyle totalValueStyle = workbook.createCellStyle();

        totalValueStyle.setFont(totalFont);
        totalValueStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );
        totalValueStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        totalValueStyle.setFillForegroundColor(
                IndexedColors.LIGHT_YELLOW.getIndex()
        );
        totalValueStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        totalValueStyle.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.##")
        );

        setThinBorders(totalValueStyle);


        /*
         * ============================================================
         * TITRE TOTAL GLOBAL
         * ============================================================
         */

        CellStyle globalTotalTitleStyle =
                workbook.createCellStyle();

        globalTotalTitleStyle.setFont(globalTitleFont);
        globalTotalTitleStyle.setAlignment(
                HorizontalAlignment.CENTER
        );
        globalTotalTitleStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        globalTotalTitleStyle.setFillForegroundColor(
                IndexedColors.DARK_GREEN.getIndex()
        );
        globalTotalTitleStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setThinBorders(globalTotalTitleStyle);


        /*
         * ============================================================
         * LABEL TOTAL GLOBAL
         * ============================================================
         */

        CellStyle globalTotalLabelStyle =
                workbook.createCellStyle();

        globalTotalLabelStyle.setFont(globalLabelFont);
        globalTotalLabelStyle.setAlignment(
                HorizontalAlignment.LEFT
        );
        globalTotalLabelStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        globalTotalLabelStyle.setFillForegroundColor(
                IndexedColors.LIGHT_GREEN.getIndex()
        );
        globalTotalLabelStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setThinBorders(globalTotalLabelStyle);

        CellStyle globalTotalValueStyle =
                workbook.createCellStyle();

        globalTotalValueStyle.setFont(totalFont);
        globalTotalValueStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );
        globalTotalValueStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        globalTotalValueStyle.setFillForegroundColor(
                IndexedColors.LIGHT_GREEN.getIndex()
        );
        globalTotalValueStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        globalTotalValueStyle.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0")
        );

        setThinBorders(globalTotalValueStyle);


        return new Styles(
                headerStyle,
                subHeaderStyle,
                abonnementStyle,
                clientStyle,
                numberStyle,
                currencyStyle,
                totalLabelStyle,
                totalValueStyle,
                globalTotalTitleStyle,
                globalTotalLabelStyle,
                globalTotalValueStyle
        );
    }

    private void setThinBorders(CellStyle style) {

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );
        style.setBottomBorderColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );
        style.setLeftBorderColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );
        style.setRightBorderColor(
                IndexedColors.GREY_50_PERCENT.getIndex()
        );
    }

    private void appliquerBordures(
            CellStyle style
    ) {

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );
    }

    private record Styles(

            CellStyle headerStyle,

            CellStyle subHeaderStyle,

            CellStyle abonnementStyle,

            CellStyle clientStyle,

            CellStyle numberStyle,

            CellStyle currencyStyle,

            CellStyle totalLabelStyle,

            CellStyle totalValueStyle,

            CellStyle globalTotalTitleStyle,

            CellStyle globalTotalLabelStyle,

            CellStyle globalTotalValueStyle

    ) {
    }
}