package com.boulangerie.reporting.service.impl;


import com.boulangerie.abonnements.dto.AbonnementConsommationReportDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleLigneDto;
import com.boulangerie.abonnements.dto.ConsommationMensuelleReportDto;
import com.boulangerie.reporting.exception.ExcelExportException;
import com.boulangerie.reporting.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final String SUMMARY_SHEET = "Sommaire";
    private static final String DETAILS_SHEET = "Détails";

    private static final String TOTAL_LABEL = "Total";
    private static final String TOTAL_QUANTITE_LABEL = "Quantité";
    private static final String TOTAL_MENSUEL_LABEL = "Montant mensuel";
    private static final String TOTAL_PAYE_LABEL = "Somme versée";
    private static final String TOTAL_RELIQUAT_LABEL = "Réliquat";

    private static final String ABONNEMENT_TITLE_PREFIX = "Abonnement : ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Locale FRENCH_LOCALE = Locale.FRENCH;

    @Override
    public byte[] exporterConsommationMensuelle(
            ConsommationMensuelleReportDto report
    ) {
        Objects.requireNonNull(
                report,
                "Le rapport mensuel est obligatoire"
        );

        YearMonth periode =
                Objects.requireNonNull(
                        report.getPeriode(),
                        "La période du rapport est obligatoire"
                );

        try (Workbook workbook = new XSSFWorkbook()) {

            Styles styles = createStyles(workbook);

            ExcelLayout layout = ExcelLayout.forPeriod(periode);

            Sheet summarySheet =
                    workbook.createSheet(SUMMARY_SHEET);

            Sheet detailsSheet =
                    workbook.createSheet(DETAILS_SHEET);

            Map<Long, Integer> lignesAbonnements =
                    écrireDetails(
                            detailsSheet,
                            report,
                            layout,
                            styles
                    );

            écrireSommaire(
                    summarySheet,
                    report,
                    lignesAbonnements,
                    styles
            );

            appliquerLargeursSommaire(
                    summarySheet
            );

            appliquerLargeursDetails(
                    detailsSheet,
                    layout
            );

            summarySheet.setPrintGridlines(false);
            detailsSheet.setPrintGridlines(false);

            workbook.setActiveSheet(
                    workbook.getSheetIndex(summarySheet)
            );

            return toBytes(workbook);

        } catch (IOException e) {
            throw new ExcelExportException(
                    "Erreur lors de la génération du fichier Excel",
                    e
            );
        }
    }

    // ============================================================
    // SOMMAIRE
    // ============================================================

    private void écrireSommaire(
            Sheet sheet,
            ConsommationMensuelleReportDto report,
            Map<Long, Integer> lignesAbonnements,
            Styles styles
    ) {
        int rowIndex = 0;

        rowIndex = écrireTitreSommaire(
                sheet,
                rowIndex,
                report,
                styles
        );

        rowIndex++;

        écrireHeaderSommaire(
                sheet,
                rowIndex,
                styles
        );

        rowIndex++;

        for (AbonnementConsommationReportDto abonnement :
                report.getAbonnements()) {

            écrireLigneSommaire(
                    sheet,
                    rowIndex,
                    abonnement,
                    lignesAbonnements,
                    styles
            );

            rowIndex++;
        }

        rowIndex++;

        écrireTotalSommaire(
                sheet,
                rowIndex,
                report,
                styles
        );

        appliquerLargeursSommaire(sheet);
    }

    private void écrireLigneSommaire(
            Sheet sheet,
            int rowIndex,
            AbonnementConsommationReportDto abonnement,
            Map<Long, Integer> lignesAbonnements,
            Styles styles
    ) {
        Row row = sheet.createRow(rowIndex);

        écrireCellule(
                row,
                SommaireLayout.ABONNEMENT,
                abonnement.getAbonnementNom(),
                styles.summaryCellStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.QUANTITE,
                abonnement.getQuantiteTotale(),
                styles.summaryNumberStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.MONTANT_MENSUEL,
                abonnement.getMontantMensuelTotal(),
                styles.summaryCurrencyStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.MONTANT_PAYE,
                abonnement.getMontantVerseTotal(),
                styles.summaryCurrencyStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.RELIQUAT,
                abonnement.getReliquatTotal(),
                styles.summaryCurrencyStyle()
        );

        Integer detailRow =
                lignesAbonnements.get(
                        abonnement.getAbonnementId()
                );

        if (detailRow != null) {
            écrireLienVersDetails(
                    row,
                    SommaireLayout.DETAIL,
                    detailRow,
                    styles
            );
        }
    }

    private int écrireTitreSommaire(
            Sheet sheet,
            int rowIndex,
            ConsommationMensuelleReportDto report,
            Styles styles
    ) {

        String mois = report.getPeriode().getMonth()
                .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                .toUpperCase(Locale.FRENCH);

        Row row = sheet.createRow(rowIndex);

        row.setHeightInPoints(30);

        Cell cell = row.createCell(SommaireLayout.ABONNEMENT);

        cell.setCellValue(
                "CONSOMMATIONS MENSUELLES - " + mois + " " + report.getPeriode().getYear()
        );

        cell.setCellStyle(
                styles.reportTitleStyle()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        SommaireLayout.ABONNEMENT,
                        SommaireLayout.LAST_COLUMN
                )
        );

        return rowIndex;
    }

    private void écrireHeaderSommaire(
            Sheet sheet,
            int rowIndex,
            Styles styles
    ) {
        Row row = sheet.createRow(rowIndex);

        écrireCellule(
                row,
                SommaireLayout.ABONNEMENT,
                "Abonnement",
                styles.summaryHeaderStyle()
        );

        écrireCellule(
                row,
                SommaireLayout.QUANTITE,
                "Quantité",
                styles.summaryHeaderStyle()
        );

        écrireCellule(
                row,
                SommaireLayout.MONTANT_MENSUEL,
                "Montant mensuel",
                styles.summaryHeaderStyle()
        );

        écrireCellule(
                row,
                SommaireLayout.MONTANT_PAYE,
                "Somme versée",
                styles.summaryHeaderStyle()
        );

        écrireCellule(
                row,
                SommaireLayout.RELIQUAT,
                "Réliquat",
                styles.summaryHeaderStyle()
        );
        écrireCellule(
                row,
                SommaireLayout.DETAIL,
                "Détail",
                styles.summaryHeaderStyle()
        );
    }

    private void écrireLienVersDetails(
            Row row,
            int column,
            int detailRow,
            Styles styles
    ) {
        Cell cell = row.createCell(column);

        cell.setCellValue("Voir");

        CreationHelper creationHelper =
                row.getSheet()
                        .getWorkbook()
                        .getCreationHelper();

        Hyperlink hyperlink =
                creationHelper.createHyperlink(
                        HyperlinkType.DOCUMENT
                );

        /*
         * Excel rows are zero-based internally,
         * but the reference uses Excel's 1-based row number.
         */
        hyperlink.setAddress(
                "'" + DETAILS_SHEET + "'!A" + (detailRow + 1)
        );

        cell.setHyperlink(hyperlink);

        cell.setCellStyle(
                styles.detailLinkStyle()
        );
    }

    private void écrireTotalSommaire(
            Sheet sheet,
            int rowIndex,
            ConsommationMensuelleReportDto report,
            Styles styles
    ) {
        Row row = sheet.createRow(rowIndex);

        écrireCellule(
                row,
                SommaireLayout.ABONNEMENT,
                "TOTAL GLOBAL",
                styles.globalTotalLabelStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.QUANTITE,
                report.getQuantiteTotale(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.MONTANT_MENSUEL,
                report.getMontantMensuelTotal(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.MONTANT_PAYE,
                report.getMontantVerseTotal(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                row,
                SommaireLayout.RELIQUAT,
                report.getReliquatTotal(),
                styles.globalTotalValueStyle()
        );
    }


    // ============================================================
    // DÉTAILS
    // ============================================================

    private Map<Long, Integer> écrireDetails(
            Sheet sheet,
            ConsommationMensuelleReportDto report,
            ExcelLayout layout,
            Styles styles
    ) {
        Map<Long, Integer> lignesAbonnements = new LinkedHashMap<>();

        int currentRow = 0;

        for (AbonnementConsommationReportDto abonnement :
                report.getAbonnements()) {

            lignesAbonnements.put(
                    abonnement.getAbonnementId(),
                    currentRow + 1
            );

            currentRow =
                    écrireAbonnement(
                            sheet,
                            currentRow,
                            report.getPeriode(),
                            abonnement,
                            layout,
                            styles
                    );

            currentRow += 2;
        }

        if (!report.getAbonnements().isEmpty()) {
            currentRow++;
        }

        écrireTotalGlobalDetails(
                sheet,
                currentRow,
                report,
                layout,
                styles
        );

        return lignesAbonnements;
    }

    private int écrireAbonnement(
            Sheet sheet,
            int rowIndex,
            YearMonth periode,
            AbonnementConsommationReportDto abonnement,
            ExcelLayout layout,
            Styles styles
    ) {
        int currentRow = rowIndex;

        currentRow =
                écrireTitreAbonnement(
                        sheet,
                        currentRow,
                        abonnement,
                        layout,
                        styles
                );

        currentRow =
                écrireEntete(
                        sheet,
                        currentRow,
                        periode,
                        layout,
                        styles
                );

        int firstClientRow =
                currentRow;

        currentRow =
                écrireLignesClients(
                        sheet,
                        currentRow,
                        abonnement,
                        layout,
                        styles
                );

        int lastClientRow =
                currentRow - 1;

        if (firstClientRow <= lastClientRow) {
            sheet.groupRow(
                    firstClientRow,
                    lastClientRow
            );
        }

        currentRow =
                écrireTotal(
                        sheet,
                        currentRow,
                        abonnement,
                        layout,
                        styles
                );

        return currentRow;
    }

    private int écrireTitreAbonnement(
            Sheet sheet,
            int rowIndex,
            AbonnementConsommationReportDto abonnement,
            ExcelLayout layout,
            Styles styles
    ) {
        Row row =
                sheet.createRow(rowIndex);

        row.setHeightInPoints(25);

        Cell titleCell =
                row.createCell(
                        layout.clientColumn()
                );

        titleCell.setCellValue(
                ABONNEMENT_TITLE_PREFIX
                        + abonnement.getAbonnementNom()
        );

        titleCell.setCellStyle(
                styles.abonnementStyle()
        );

        Cell abonnementIdCell =
                row.createCell(
                        layout.abonnementIdColumn()
                );

        abonnementIdCell.setCellValue(
                abonnement.getAbonnementId()
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        layout.clientColumn(),
                        layout.clientIdColumn()
                )
        );

        return rowIndex + 1;
    }

    private int écrireEntete(
            Sheet sheet,
            int rowIndex,
            YearMonth periode,
            ExcelLayout layout,
            Styles styles
    ) {
        Row headerRow =
                sheet.createRow(rowIndex);

        Row daysRow =
                sheet.createRow(rowIndex + 1);

        écrireCelluleEntete(
                headerRow,
                layout.clientColumn(),
                "Client",
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
                    layout.firstDayColumn()
                            + jour
                            - 1;

            écrireCelluleEntete(
                    headerRow,
                    column,
                    DATE_FORMATTER.format(date),
                    styles
            );

            écrireCellule(
                    daysRow,
                    column,
                    capitalize(
                            date.getDayOfWeek()
                                    .getDisplayName(
                                            TextStyle.FULL,
                                            FRENCH_LOCALE
                                    )
                    ),
                    styles.subHeaderStyle()
            );
        }

        écrireCelluleEntete(
                headerRow,
                layout.totalColumn(),
                "Total",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.prixUnitaireColumn(),
                "P.unitaire",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.montantMensuelColumn(),
                "Montant mensuel",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.montantPayeColumn(),
                "Somme versée",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.reliquatColumn(),
                "Réliquat",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.abonnementIdColumn(),
                "Abonnement ID",
                styles
        );

        écrireCelluleEntete(
                headerRow,
                layout.clientIdColumn(),
                "Client ID",
                styles
        );

        /*
         * La colonne Client couvre visuellement
         * les deux lignes d'en-tête.
         */
        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex + 1,
                        layout.clientColumn(),
                        layout.clientColumn()
                )
        );

        return rowIndex + 2;
    }

    private int écrireLignesClients(
            Sheet sheet,
            int rowIndex,
            AbonnementConsommationReportDto abonnement,
            ExcelLayout layout,
            Styles styles
    ) {
        int currentRow = rowIndex;

        for (ConsommationMensuelleLigneDto ligne :
                abonnement.getLignes()) {

            Row row =
                    sheet.createRow(currentRow++);

            écrireCellule(
                    row,
                    layout.clientColumn(),
                    construireNomClient(ligne),
                    styles.clientStyle()
            );

            écrireConsommations(
                    row,
                    ligne,
                    layout,
                    styles
            );

            écrireNombre(
                    row,
                    layout.totalColumn(),
                    ligne.getQuantiteTotale(),
                    styles.numberStyle()
            );

            écrireNombre(
                    row,
                    layout.prixUnitaireColumn(),
                    ligne.getPrixUnitaire(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    layout.montantMensuelColumn(),
                    ligne.getMontant(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    layout.montantPayeColumn(),
                    ligne.getMontantPaye(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    layout.reliquatColumn(),
                    ligne.getReliquat(),
                    styles.currencyStyle()
            );

            écrireNombre(
                    row,
                    layout.abonnementIdColumn(),
                    BigDecimal.valueOf(abonnement.getAbonnementId()),
                    styles.numberStyle()
            );

            écrireNombre(
                    row,
                    layout.clientIdColumn(),
                    BigDecimal.valueOf(ligne.getClientId()),
                    styles.numberStyle()
            );
        }

        return currentRow;
    }

    private void écrireConsommations(
            Row row,
            ConsommationMensuelleLigneDto ligne,
            ExcelLayout layout,
            Styles styles
    ) {
        if (ligne.getConsommations() == null) {
            return;
        }

        for (
                Map.Entry<Integer, BigDecimal> entry :
                ligne.getConsommations().entrySet()
        ) {
            Integer jour =
                    entry.getKey();

            if (
                    jour == null
                            || jour < 1
                            || jour > layout.nombreJours()
            ) {
                continue;
            }

            BigDecimal quantite =
                    entry.getValue();

            if (quantite == null) {
                continue;
            }

            int column =
                    layout.firstDayColumn()
                            + jour
                            - 1;

            Cell cell =
                    row.createCell(column);

            /*
             * Une consommation nulle reste visuellement vide.
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

    // ============================================================
    // TOTAL D'UN ABONNEMENT
    // ============================================================

    private int écrireTotal(
            Sheet sheet,
            int rowIndex,
            AbonnementConsommationReportDto abonnement,
            ExcelLayout layout,
            Styles styles
    ) {
        Row row =
                sheet.createRow(rowIndex);

        /*
         * Libellé.
         */
        écrireCellule(
                row,
                layout.clientColumn(),
                TOTAL_LABEL,
                styles.totalLabelStyle()
        );

        /*
         * Colonnes journalières :
         * elles restent vides, mais gardent le style
         * de total afin de fermer visuellement le tableau.
         */
        for (
                int column = layout.firstDayColumn();
                column < layout.totalColumn();
                column++
        ) {
            écrireCellule(
                    row,
                    column,
                    "",
                    styles.totalLabelStyle()
            );
        }

        /*
         * Quantité totale.
         */
        écrireNombre(
                row,
                layout.totalColumn(),
                abonnement.getQuantiteTotale(),
                styles.totalValueStyle()
        );

        /*
         * Le prix unitaire n'a pas de sens au niveau
         * du total de l'abonnement.
         */
        écrireCellule(
                row,
                layout.prixUnitaireColumn(),
                "",
                styles.totalValueStyle()
        );

        /*
         * Montant mensuel.
         */
        écrireNombre(
                row,
                layout.montantMensuelColumn(),
                abonnement.getMontantMensuelTotal(),
                styles.totalValueStyle()
        );

        /*
         * Somme versée.
         */
        écrireNombre(
                row,
                layout.montantPayeColumn(),
                abonnement.getMontantVerseTotal(),
                styles.totalValueStyle()
        );

        /*
         * Réliquat.
         */
        écrireNombre(
                row,
                layout.reliquatColumn(),
                abonnement.getReliquatTotal(),
                styles.totalValueStyle()
        );

        écrireNombre(
                row,
                layout.abonnementIdColumn(),
                BigDecimal.valueOf(abonnement.getAbonnementId()),
                styles.totalValueStyle()
        );

        écrireCellule(
                row,
                layout.clientIdColumn(),
                "",
                styles.totalValueStyle()
        );

        return rowIndex + 1;
    }

    // ============================================================
    // TOTAL GLOBAL
    // ============================================================

    private void écrireTotalGlobalDetails(
            Sheet sheet,
            int rowIndex,
            ConsommationMensuelleReportDto report,
            ExcelLayout layout,
            Styles styles
    ) {
        Row titleRow =
                sheet.createRow(rowIndex);

        titleRow.setHeightInPoints(30);

        Cell titleCell =
                titleRow.createCell(
                        layout.clientColumn()
                );

        titleCell.setCellValue(
                "TOTAL GLOBAL - " + report.getPeriode()
        );

        titleCell.setCellStyle(
                styles.globalTotalTitleStyle()
        );

        /*
         * The global title spans all VISIBLE columns.
         *
         * A = Client
         * B:AF = days
         * AG = Total
         * AH = P.U.
         * AI = Montant mensuel
         * AJ = Somme versée
         * AK = Réliquat
         *
         * AL and AM are technical IDs and are hidden.
         */
        int lastVisibleColumn =
                layout.reliquatColumn();

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        layout.clientColumn(),
                        lastVisibleColumn
                )
        );

        Row labelRow =
                sheet.createRow(rowIndex + 1);

        écrireCellule(
                labelRow,
                0,
                TOTAL_QUANTITE_LABEL,
                styles.globalTotalLabelStyle()
        );

        écrireCellule(
                labelRow,
                1,
                TOTAL_MENSUEL_LABEL,
                styles.globalTotalLabelStyle()
        );

        écrireCellule(
                labelRow,
                2,
                TOTAL_PAYE_LABEL,
                styles.globalTotalLabelStyle()
        );

        écrireCellule(
                labelRow,
                3,
                TOTAL_RELIQUAT_LABEL,
                styles.globalTotalLabelStyle()
        );

        Row valueRow =
                sheet.createRow(rowIndex + 2);

        écrireNombre(
                valueRow,
                0,
                report.getQuantiteTotale(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                valueRow,
                1,
                report.getMontantMensuelTotal(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                valueRow,
                2,
                report.getMontantVerseTotal(),
                styles.globalTotalValueStyle()
        );

        écrireNombre(
                valueRow,
                3,
                report.getReliquatTotal(),
                styles.globalTotalValueStyle()
        );
    }
    // ============================================================
    // CELLULES
    // ============================================================

    private void écrireCellule(
            Row row,
            int column,
            String value,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(column);

        cell.setCellValue(
                value == null ? "" : value
        );

        cell.setCellStyle(style);
    }

    private void écrireNombre(
            Row row,
            int column,
            BigDecimal value,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(column);

        cell.setCellValue(
                value == null
                        ? 0D
                        : value.doubleValue()
        );

        cell.setCellStyle(style);
    }

    private void écrireCelluleEntete(
            Row row,
            int column,
            String value,
            Styles styles
    ) {
        écrireCellule(
                row,
                column,
                value,
                styles.headerStyle()
        );
    }

    private void écrireLien(
            Row row,
            int column,
            String value,
            String sheetName,
            int excelRow,
            CellStyle style
    ) {
        Cell cell =
                row.createCell(column);

        cell.setCellValue(value);

        Hyperlink hyperlink =
                cell.getSheet()
                        .getWorkbook()
                        .getCreationHelper()
                        .createHyperlink(
                                HyperlinkType.DOCUMENT
                        );

        hyperlink.setAddress(
                "'" + sheetName + "'!A" + excelRow
        );

        cell.setHyperlink(hyperlink);
        cell.setCellStyle(style);
    }

    // ============================================================
    // LARGEURS
    // ============================================================

    private void appliquerLargeursSommaire(Sheet sheet) {

        sheet.setColumnWidth(
                SommaireLayout.ABONNEMENT,
                32 * 256
        );

        sheet.setColumnWidth(
                SommaireLayout.QUANTITE,
                16 * 256
        );

        sheet.setColumnWidth(
                SommaireLayout.MONTANT_MENSUEL,
                20 * 256
        );

        sheet.setColumnWidth(
                SommaireLayout.MONTANT_PAYE,
                18 * 256
        );

        sheet.setColumnWidth(
                SommaireLayout.RELIQUAT,
                18 * 256
        );
        sheet.setColumnWidth(
                SommaireLayout.DETAIL,
                12 * 256
        );
    }

    private void appliquerLargeursDetails(
            Sheet sheet,
            ExcelLayout layout
    ) {
        sheet.setColumnWidth(
                layout.clientColumn(),
                30 * 256
        );

        for (
                int jour = 1;
                jour <= layout.nombreJours();
                jour++
        ) {
            sheet.setColumnWidth(
                    layout.firstDayColumn() + jour - 1,
                    13 * 256
            );
        }

        sheet.setColumnWidth(
                layout.totalColumn(),
                14 * 256
        );

        sheet.setColumnWidth(
                layout.prixUnitaireColumn(),
                14 * 256
        );

        sheet.setColumnWidth(
                layout.montantMensuelColumn(),
                18 * 256
        );

        sheet.setColumnWidth(
                layout.montantPayeColumn(),
                16 * 256
        );

        sheet.setColumnWidth(
                layout.reliquatColumn(),
                16 * 256
        );


        /*
         * IMPORTANT:
         * Keep IDs in the workbook but hide them from the business user.
         */
        sheet.setColumnHidden(
                layout.abonnementIdColumn(),
                true
        );

        sheet.setColumnHidden(
                layout.clientIdColumn(),
                true
        );
    }


    // ============================================================
    // UTILITAIRES
    // ============================================================

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
            return nom == null
                    ? ""
                    : nom;
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
                .toUpperCase(FRENCH_LOCALE)
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

    // ============================================================
    // STYLES
    // ============================================================

    private Styles createStyles(Workbook workbook) {

        XSSFWorkbook xssfWorkbook =
                (XSSFWorkbook) workbook;

        DataFormat dataFormat =
                workbook.createDataFormat();

        // ============================================================
        // BAKERY PALETTE
        // ============================================================

        XSSFColor chocolate =
                rgb(92, 51, 23);

        XSSFColor darkChocolate =
                rgb(62, 39, 22);

        XSSFColor beige =
                rgb(239, 226, 202);

        XSSFColor lightBeige =
                rgb(248, 242, 232);

        XSSFColor white =
                rgb(255, 255, 255);

        XSSFColor borderColor =
                rgb(210, 195, 175);

        // ============================================================
        // FONTS
        // ============================================================

        XSSFFont headerFont =
                createFont(
                        xssfWorkbook,
                        white,
                        true,
                        10
                );

        XSSFFont abonnementFont =
                createFont(
                        xssfWorkbook,
                        white,
                        true,
                        13
                );

        XSSFFont totalFont =
                createFont(
                        xssfWorkbook,
                        darkChocolate,
                        true,
                        10
                );

        XSSFFont reportTitleFont =
                createFont(
                        xssfWorkbook,
                        darkChocolate,
                        true,
                        16
                );

        XSSFFont globalTitleFont =
                createFont(
                        xssfWorkbook,
                        white,
                        true,
                        13
                );

        XSSFFont normalFont =
                createFont(
                        xssfWorkbook,
                        darkChocolate,
                        false,
                        10
                );

        // ============================================================
        // DÉTAILS - HEADER
        // ============================================================

        XSSFCellStyle headerStyle =
                createBaseStyle(
                        xssfWorkbook,
                        chocolate,
                        borderColor
                );

        headerStyle.setFont(headerFont);

        headerStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        headerStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        headerStyle.setWrapText(true);

        // ============================================================
        // DÉTAILS - SUB HEADER / DAYS
        // ============================================================

        XSSFCellStyle subHeaderStyle =
                createBaseStyle(
                        xssfWorkbook,
                        beige,
                        borderColor
                );

        subHeaderStyle.setFont(
                createFont(
                        xssfWorkbook,
                        darkChocolate,
                        true,
                        9
                )
        );

        subHeaderStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        subHeaderStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        subHeaderStyle.setWrapText(true);

        // ============================================================
        // DÉTAILS - ABONNEMENT TITLE
        // ============================================================

        XSSFCellStyle abonnementStyle =
                createBaseStyle(
                        xssfWorkbook,
                        chocolate,
                        borderColor
                );

        abonnementStyle.setFont(
                abonnementFont
        );

        abonnementStyle.setAlignment(
                HorizontalAlignment.LEFT
        );

        abonnementStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // DÉTAILS - CLIENT
        // ============================================================

        XSSFCellStyle clientStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        clientStyle.setFont(
                normalFont
        );

        clientStyle.setAlignment(
                HorizontalAlignment.LEFT
        );

        clientStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // DÉTAILS - NUMBER
        // ============================================================

        XSSFCellStyle numberStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        numberStyle.setFont(
                normalFont
        );

        numberStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );

        numberStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        numberStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.##"
                )
        );

        // ============================================================
        // DÉTAILS - CURRENCY
        // ============================================================

        XSSFCellStyle currencyStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        currencyStyle.setFont(
                normalFont
        );

        currencyStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );

        currencyStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        currencyStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0"
                )
        );

        // ============================================================
        // DÉTAILS - TOTAL LABEL
        // ============================================================

        XSSFCellStyle totalLabelStyle =
                createBaseStyle(
                        xssfWorkbook,
                        beige,
                        borderColor
                );

        totalLabelStyle.setFont(
                totalFont
        );

        totalLabelStyle.setAlignment(
                HorizontalAlignment.LEFT
        );

        totalLabelStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // DÉTAILS - TOTAL VALUE
        // ============================================================

        XSSFCellStyle totalValueStyle =
                createBaseStyle(
                        xssfWorkbook,
                        beige,
                        borderColor
                );

        totalValueStyle.setFont(
                totalFont
        );

        totalValueStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );

        totalValueStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        totalValueStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.##"
                )
        );

        // ============================================================
        // REPORT TITLE
        // ============================================================

        XSSFCellStyle reportTitleStyle =
                createBaseStyle(
                        xssfWorkbook,
                        lightBeige,
                        borderColor
                );

        reportTitleStyle.setFont(
                reportTitleFont
        );

        reportTitleStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        reportTitleStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // SOMMAIRE - HEADER
        // ============================================================

        XSSFCellStyle summaryHeaderStyle =
                createBaseStyle(
                        xssfWorkbook,
                        chocolate,
                        borderColor
                );

        summaryHeaderStyle.setFont(
                headerFont
        );

        summaryHeaderStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        summaryHeaderStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        summaryHeaderStyle.setWrapText(true);

        // ============================================================
        // SOMMAIRE - TEXT
        // ============================================================

        XSSFCellStyle summaryCellStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        summaryCellStyle.setFont(
                normalFont
        );

        summaryCellStyle.setAlignment(
                HorizontalAlignment.LEFT
        );

        summaryCellStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // SOMMAIRE - NUMBER
        // ============================================================

        XSSFCellStyle summaryNumberStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        summaryNumberStyle.setFont(
                normalFont
        );

        summaryNumberStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );

        summaryNumberStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        summaryNumberStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.##"
                )
        );

        // ============================================================
        // SOMMAIRE - CURRENCY
        // ============================================================

        XSSFCellStyle summaryCurrencyStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        summaryCurrencyStyle.setFont(
                normalFont
        );

        summaryCurrencyStyle.setAlignment(
                HorizontalAlignment.RIGHT
        );

        summaryCurrencyStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        summaryCurrencyStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0"
                )
        );

        // ============================================================
        // DETAIL - HEADER
        // ============================================================
        XSSFCellStyle detailLinkStyle =
                createBaseStyle(
                        xssfWorkbook,
                        white,
                        borderColor
                );

        XSSFFont linkFont =
                createFont(
                        xssfWorkbook,
                        rgb(92, 51, 23),
                        true,
                        10
                );

        linkFont.setUnderline(
                FontUnderline.SINGLE
        );

        detailLinkStyle.setFont(linkFont);

        detailLinkStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        detailLinkStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // GLOBAL TOTAL - TITLE
        // ============================================================

        XSSFCellStyle globalTotalTitleStyle =
                createBaseStyle(
                        xssfWorkbook,
                        chocolate,
                        borderColor
                );

        globalTotalTitleStyle.setFont(
                globalTitleFont
        );

        /*
         * This is important.
         *
         * The merged region will span the complete visible
         * Details area, so this style centers the title
         * inside that region.
         */


        globalTotalTitleStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // GLOBAL TOTAL - LABEL
        // ============================================================

        XSSFCellStyle globalTotalLabelStyle =
                createBaseStyle(
                        xssfWorkbook,
                        beige,
                        borderColor
                );

        globalTotalLabelStyle.setFont(
                totalFont
        );


        globalTotalLabelStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        // ============================================================
        // GLOBAL TOTAL - VALUE
        // ============================================================

        XSSFCellStyle globalTotalValueStyle =
                createBaseStyle(
                        xssfWorkbook,
                        lightBeige,
                        borderColor
                );

        globalTotalValueStyle.setFont(
                totalFont
        );



        globalTotalValueStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        globalTotalValueStyle.setDataFormat(
                dataFormat.getFormat(
                        "#,##0.##"
                )
        );

        return new Styles(
                headerStyle,
                subHeaderStyle,
                abonnementStyle,
                clientStyle,
                numberStyle,
                currencyStyle,
                totalLabelStyle,
                totalValueStyle,
                reportTitleStyle,
                summaryHeaderStyle,
                summaryCellStyle,
                summaryNumberStyle,
                summaryCurrencyStyle,
                detailLinkStyle,
                globalTotalTitleStyle,
                globalTotalLabelStyle,
                globalTotalValueStyle
        );
    }

    private XSSFCellStyle createBaseStyle(
            XSSFWorkbook workbook,
            XSSFColor background,
            XSSFColor borderColor
    ) {
        XSSFCellStyle style =
                workbook.createCellStyle();

        style.setFillForegroundColor(
                background
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        setBorders(
                style,
                borderColor
        );

        return style;
    }

    private XSSFColor rgb(
            int red,
            int green,
            int blue
    ) {
        return new XSSFColor(
                new byte[]{
                        (byte) red,
                        (byte) green,
                        (byte) blue
                },
                null
        );
    }

    private XSSFFont createFont(
            XSSFWorkbook workbook,
            XSSFColor color,
            boolean bold,
            int size
    ) {
        XSSFFont font =
                workbook.createFont();

        font.setColor(color);
        font.setBold(bold);
        font.setFontHeightInPoints(
                (short) size
        );

        return font;
    }

    private void setBorders(
            XSSFCellStyle style,
            XSSFColor color
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

        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }
    private void setThinBorders(
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

        short color =
                IndexedColors.GREY_50_PERCENT.getIndex();

        style.setTopBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
        style.setRightBorderColor(color);
    }

    // ============================================================
    // INTERNAL MODELS
    // ============================================================

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

    private record Styles(
            XSSFCellStyle headerStyle,
            XSSFCellStyle subHeaderStyle,
            XSSFCellStyle abonnementStyle,

            XSSFCellStyle clientStyle,
            XSSFCellStyle numberStyle,
            XSSFCellStyle currencyStyle,

            XSSFCellStyle totalLabelStyle,
            XSSFCellStyle totalValueStyle,

            XSSFCellStyle reportTitleStyle,

            XSSFCellStyle summaryHeaderStyle,
            XSSFCellStyle summaryCellStyle,
            XSSFCellStyle summaryNumberStyle,
            XSSFCellStyle summaryCurrencyStyle,
            XSSFCellStyle detailLinkStyle,
            XSSFCellStyle globalTotalTitleStyle,
            XSSFCellStyle globalTotalLabelStyle,
            XSSFCellStyle globalTotalValueStyle
    ) {
    }

    public static final class SommaireLayout {

        public static final int ABONNEMENT = 0;
        public static final int QUANTITE = 1;
        public static final int MONTANT_MENSUEL = 2;
        public static final int MONTANT_PAYE = 3;
        public static final int RELIQUAT = 4;
        public static final int DETAIL = 5;

        public static final int LAST_COLUMN = DETAIL;

        private SommaireLayout() {
        }
    }

    public static final class DetailsLayout {

        public static final int CLIENT = 0;

        public static final int FIRST_DAY = 1;

        public static final int TOTAL = 32;
        public static final int PRIX_UNITAIRE = 33;
        public static final int MONTANT_MENSUEL = 34;
        public static final int MONTANT_PAYE = 35;
        public static final int RELIQUAT = 36;

        public static final int ABONNEMENT_ID = 37;
        public static final int CLIENT_ID = 38;

        public static final int LAST_VISIBLE_COLUMN = RELIQUAT;
        public static final int LAST_COLUMN = CLIENT_ID;

        private DetailsLayout() {
        }
    }
}