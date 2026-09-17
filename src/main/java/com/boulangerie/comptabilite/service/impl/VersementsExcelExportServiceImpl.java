package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.comptabilite.dto.LigneMontantDto;
import com.boulangerie.comptabilite.dto.VersementsJourDto;
import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;
import com.boulangerie.comptabilite.service.VersementsExcelExportService;
import com.boulangerie.shared.exception.ExcelExportException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exporteur Excel du rapport mensuel VERSEMENTS/FRAIS.
 *
 * Contrairement au rapport de production (une matrice à colonnes
 * fixes par jour), ce rapport est une liste SÉQUENTIELLE de blocs
 * journaliers de hauteur VARIABLE : chaque jour occupe exactement
 * autant de lignes que nécessaire pour ses versements/frais (le
 * plus grand des deux), plus une ligne "Total". Rien n'est
 * hardcodé ni padded à une taille fixe — la position de chaque
 * ligne est calculée au moment de l'écriture, à partir du contenu
 * réel du jour précédent. C'est ce qui permet au fichier de
 * s'adapter à n'importe quel mois, avec n'importe quel nombre
 * d'entrées par jour, sans jamais désaligner les totaux.
 */
@Service
public class VersementsExcelExportServiceImpl
        implements VersementsExcelExportService {

    private static final String SHEET_NAME = "Feuil1";

    private static final int COL_DATE = 0;
    private static final int COL_VERSEMENT_LABEL = 1;
    private static final int COL_VERSEMENT_MONTANT = 2;
    private static final int COL_FRAIS_LABEL = 3;
    private static final int COL_FRAIS_MONTANT = 4;

    private static final int HEADER_ROW = 0;
    private static final int FIRST_DATA_ROW = 1;

    private static final String DATE_FORMAT = "dd-mm-yyyy";
    private static final String MONTANT_FORMAT = "#,##0 \"CFA\"";

    private static final String COLOR_TOTAL_VERSEMENTS = "92D050"; // vert
    private static final String COLOR_TOTAL_FRAIS = "FF0000";      // rouge
    private static final String COLOR_GRAND_TOTAL_VERSEMENTS = "00B0F0"; // bleu
    private static final String COLOR_GRAND_TOTAL_FRAIS = "ED7D31";      // orange
    private static final String COLOR_NET = "FFFF00";                    // jaune

    @Override
    public byte[] exporterVersementsMensuel(
            VersementsRapportMensuelDto rapport
    ) {

        Objects.requireNonNull(
                rapport,
                "Le rapport de versements est obligatoire."
        );

        Objects.requireNonNull(
                rapport.getPeriode(),
                "La période est obligatoire."
        );

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            Styles styles = createStyles(workbook);

            ecrireEntete(sheet, styles);

            /*
             * Lignes Excel (1-based) de chaque total journalier,
             * dans l'ordre — nécessaires pour construire la
             * formule de la somme totale mensuelle, puisque ces
             * lignes ne sont PAS contiguës (entrecoupées par les
             * lignes de données du jour suivant). C'est exactement
             * la même approche que le fichier de référence
             * (=SUM(C378,C397,C415,...)), simplement calculée au
             * lieu d'être tapée à la main.
             */
            List<Integer> lignesTotalExcel = new ArrayList<>();

            int currentRow = FIRST_DATA_ROW;

            for (VersementsJourDto jour : rapport.getJours()) {

                currentRow = ecrireJour(
                        sheet,
                        currentRow,
                        jour,
                        styles,
                        lignesTotalExcel
                );
            }

            ecrireTotauxMensuels(
                    sheet,
                    currentRow,
                    lignesTotalExcel,
                    styles
            );

            configurerFeuille(sheet);

            return toBytes(workbook);

        } catch (IOException e) {

            throw new ExcelExportException(
                    "Erreur lors de la génération du fichier "
                            + "Excel des versements.",
                    e
            );
        }
    }

    /*
     * ============================================================
     * EN-TÊTE
     * ============================================================
     */

    private void ecrireEntete(Sheet sheet, Styles styles) {

        Row header = sheet.createRow(HEADER_ROW);

        Cell date = header.createCell(COL_DATE);
        date.setCellValue("Date");
        date.setCellStyle(styles.headerPlain);

        Cell versements = header.createCell(COL_VERSEMENT_LABEL);
        versements.setCellValue("VERSEMENTS");
        versements.setCellStyle(styles.headerBold);

        Cell frais = header.createCell(COL_FRAIS_LABEL);
        frais.setCellValue("FRAIS");
        frais.setCellStyle(styles.headerBold);
    }

    /*
     * ============================================================
     * UN JOUR = UN BLOC DE HAUTEUR VARIABLE
     * ============================================================
     *
     * Retourne la première ligne libre APRÈS ce bloc (à passer
     * comme currentRow pour le jour suivant).
     */
    private int ecrireJour(
            Sheet sheet,
            int firstRow,
            VersementsJourDto jour,
            Styles styles,
            List<Integer> lignesTotalExcel
    ) {

        List<LigneMontantDto> versements = jour.getVersements();
        List<LigneMontantDto> frais = jour.getFrais();

        int nbLignes = Math.max(versements.size(), frais.size());

        if (nbLignes == 0) {
            // Aucune activité ce jour-là : rien à écrire.
            return firstRow;
        }

        Cell dateCell =
                getOrCreateRow(sheet, firstRow).createCell(COL_DATE);

        dateCell.setCellValue(
                java.sql.Date.valueOf(jour.getDate())
        );

        dateCell.setCellStyle(styles.date);

        for (int i = 0; i < nbLignes; i++) {

            int row = firstRow + i;

            if (i < versements.size()) {
                ecrireLigneMontant(
                        sheet,
                        row,
                        COL_VERSEMENT_LABEL,
                        COL_VERSEMENT_MONTANT,
                        versements.get(i),
                        styles
                );
            }

            if (i < frais.size()) {
                ecrireLigneMontant(
                        sheet,
                        row,
                        COL_FRAIS_LABEL,
                        COL_FRAIS_MONTANT,
                        frais.get(i),
                        styles
                );
            }
        }

        int totalRow = firstRow + nbLignes;
        int firstDataExcelRow = firstRow + 1; // 0-based -> 1-based
        int totalExcelRow = totalRow + 1;

        ecrireTotalJournalier(
                sheet,
                totalRow,
                COL_VERSEMENT_LABEL,
                COL_VERSEMENT_MONTANT,
                firstDataExcelRow,
                totalExcelRow,
                styles.totalVersementsLabel,
                styles.totalVersementsMontant
        );

        ecrireTotalJournalier(
                sheet,
                totalRow,
                COL_FRAIS_LABEL,
                COL_FRAIS_MONTANT,
                firstDataExcelRow,
                totalExcelRow,
                styles.totalFraisLabel,
                styles.totalFraisMontant
        );

        lignesTotalExcel.add(totalExcelRow);

        return totalRow + 1;
    }

    private void ecrireLigneMontant(
            Sheet sheet,
            int row,
            int labelColumn,
            int montantColumn,
            LigneMontantDto ligne,
            Styles styles
    ) {

        Row r = getOrCreateRow(sheet, row);

        Cell labelCell = r.createCell(labelColumn);
        labelCell.setCellValue(nullSafe(ligne.getLibelle()));
        labelCell.setCellStyle(styles.label);

        Cell montantCell = r.createCell(montantColumn);
        montantCell.setCellValue(safe(ligne.getMontant()).doubleValue());
        montantCell.setCellStyle(styles.montant);
    }

    private void ecrireTotalJournalier(
            Sheet sheet,
            int totalRow,
            int labelColumn,
            int montantColumn,
            int firstDataExcelRow,
            int totalExcelRow,
            CellStyle labelStyle,
            CellStyle montantStyle
    ) {

        Row row = getOrCreateRow(sheet, totalRow);

        Cell labelCell = row.createCell(labelColumn);
        labelCell.setCellValue("Total");
        labelCell.setCellStyle(labelStyle);

        Cell montantCell = row.createCell(montantColumn);

        montantCell.setCellFormula(
                "SUM("
                        + columnLetter(montantColumn)
                        + firstDataExcelRow
                        + ":"
                        + columnLetter(montantColumn)
                        + (totalExcelRow - 1)
                        + ")"
        );

        montantCell.setCellStyle(montantStyle);
    }

    /*
     * ============================================================
     * TOTAUX MENSUELS
     * ============================================================
     */

    private void ecrireTotauxMensuels(
            Sheet sheet,
            int startRow,
            List<Integer> lignesTotalExcel,
            Styles styles
    ) {

        if (lignesTotalExcel.isEmpty()) {
            // Mois sans aucune activité : rien à totaliser.
            return;
        }

        int ligneSommeGlobale = startRow;
        int ligneNet = startRow + 1;

        Row sommeRow = getOrCreateRow(sheet, ligneSommeGlobale);

        Cell libelleVersements =
                sommeRow.createCell(COL_VERSEMENT_LABEL);
        libelleVersements.setCellValue("SOMME TOTALE DES VERSEMENTS");
        libelleVersements.setCellStyle(styles.grandTotalVersementsLabel);

        Cell montantVersements =
                sommeRow.createCell(COL_VERSEMENT_MONTANT);
        montantVersements.setCellFormula(
                sommeDesLignes(COL_VERSEMENT_MONTANT, lignesTotalExcel)
        );
        montantVersements.setCellStyle(styles.grandTotalVersementsMontant);

        Cell libelleFrais = sommeRow.createCell(COL_FRAIS_LABEL);
        libelleFrais.setCellValue("SOMME TOTALE DES FRAIS");
        libelleFrais.setCellStyle(styles.grandTotalFraisLabel);

        Cell montantFrais = sommeRow.createCell(COL_FRAIS_MONTANT);
        montantFrais.setCellFormula(
                sommeDesLignes(COL_FRAIS_MONTANT, lignesTotalExcel)
        );
        montantFrais.setCellStyle(styles.grandTotalFraisMontant);

        Row netRow = getOrCreateRow(sheet, ligneNet);

        Cell libelleNet = netRow.createCell(COL_VERSEMENT_LABEL);
        libelleNet.setCellValue("Total");
        libelleNet.setCellStyle(styles.netLabel);

        Cell montantNet = netRow.createCell(COL_VERSEMENT_MONTANT);

        montantNet.setCellFormula(
                cellReference(COL_VERSEMENT_MONTANT, ligneSommeGlobale)
                        + "-"
                        + cellReference(COL_FRAIS_MONTANT, ligneSommeGlobale)
        );

        montantNet.setCellStyle(styles.netMontant);
    }

    /*
     * Construit "SUM(C14,C27,C40,...)" à partir de la liste des
     * lignes de total journalier (déjà en numérotation Excel
     * 1-based).
     */
    private String sommeDesLignes(int column, List<Integer> lignesExcel) {

        StringBuilder formule = new StringBuilder("SUM(");

        for (int i = 0; i < lignesExcel.size(); i++) {

            if (i > 0) {
                formule.append(",");
            }

            formule.append(columnLetter(column))
                    .append(lignesExcel.get(i));
        }

        return formule.append(")").toString();
    }

    private String cellReference(int column, int zeroBasedRow) {
        return columnLetter(column) + (zeroBasedRow + 1);
    }

    /*
     * ============================================================
     * CONFIGURATION FEUILLE
     * ============================================================
     */

    private void configurerFeuille(Sheet sheet) {

        sheet.createFreezePane(1, 1);

        sheet.setColumnWidth(COL_DATE, 21 * 256);
        sheet.setColumnWidth(COL_VERSEMENT_LABEL, 57 * 256);
        sheet.setColumnWidth(COL_VERSEMENT_MONTANT, 25 * 256);
        sheet.setColumnWidth(COL_FRAIS_LABEL, 54 * 256);
        sheet.setColumnWidth(COL_FRAIS_MONTANT, 21 * 256);
    }

    /*
     * ============================================================
     * HELPERS
     * ============================================================
     */

    private Row getOrCreateRow(Sheet sheet, int rowIndex) {

        Row row = sheet.getRow(rowIndex);

        return row != null ? row : sheet.createRow(rowIndex);
    }

    private String columnLetter(int zeroBasedColumn) {

        int column = zeroBasedColumn + 1;
        StringBuilder result = new StringBuilder();

        while (column > 0) {

            int remainder = (column - 1) % 26;
            result.insert(0, (char) ('A' + remainder));
            column = (column - 1) / 26;
        }

        return result.toString();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private byte[] toBytes(Workbook workbook) throws IOException {

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.write(output);
            return output.toByteArray();
        }
    }

    /*
     * ============================================================
     * STYLES
     * ============================================================
     */

    private Styles createStyles(Workbook workbook) {

        Styles styles = new Styles();

        styles.headerPlain = baseStyle(workbook, false);
        styles.headerBold = baseStyle(workbook, true);

        styles.date = baseStyle(workbook, false);
        styles.date.setDataFormat(
                workbook.createDataFormat().getFormat(DATE_FORMAT)
        );

        styles.label = baseStyle(workbook, false);

        styles.montant = baseStyle(workbook, false);
        styles.montant.setDataFormat(
                workbook.createDataFormat().getFormat(MONTANT_FORMAT)
        );

        styles.totalVersementsLabel = baseStyle(workbook, false);
        fill(styles.totalVersementsLabel, COLOR_TOTAL_VERSEMENTS);

        styles.totalVersementsMontant = montantStyle(workbook, false);
        fill(styles.totalVersementsMontant, COLOR_TOTAL_VERSEMENTS);

        styles.totalFraisLabel = baseStyle(workbook, false);
        fill(styles.totalFraisLabel, COLOR_TOTAL_FRAIS);

        styles.totalFraisMontant = montantStyle(workbook, false);
        fill(styles.totalFraisMontant, COLOR_TOTAL_FRAIS);

        // Le fichier de référence a "SOMME TOTALE DES VERSEMENTS" non
        // gras mais "SOMME TOTALE DES FRAIS" en gras : reproduit tel
        // quel plutôt qu'harmonisé, pour matcher exactement.
        styles.grandTotalVersementsLabel = baseStyle(workbook, false);
        fill(
                styles.grandTotalVersementsLabel,
                COLOR_GRAND_TOTAL_VERSEMENTS
        );

        styles.grandTotalVersementsMontant = montantStyle(workbook, false);
        fill(
                styles.grandTotalVersementsMontant,
                COLOR_GRAND_TOTAL_VERSEMENTS
        );

        styles.grandTotalFraisLabel = baseStyle(workbook, true);
        fill(styles.grandTotalFraisLabel, COLOR_GRAND_TOTAL_FRAIS);

        styles.grandTotalFraisMontant = montantStyle(workbook, true);
        fill(styles.grandTotalFraisMontant, COLOR_GRAND_TOTAL_FRAIS);

        styles.netLabel = baseStyle(workbook, false);
        fill(styles.netLabel, COLOR_NET);

        styles.netMontant = montantStyle(workbook, false);
        fill(styles.netMontant, COLOR_NET);

        return styles;
    }

    private CellStyle baseStyle(Workbook workbook, boolean bold) {

        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setVerticalAlignment(VerticalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(bold);
        style.setFont(font);

        return style;
    }

    private CellStyle montantStyle(Workbook workbook, boolean bold) {

        CellStyle style = baseStyle(workbook, bold);

        style.setDataFormat(
                workbook.createDataFormat().getFormat(MONTANT_FORMAT)
        );

        return style;
    }

    private void fill(CellStyle style, String hexRgb) {

        XSSFCellStyle xssfStyle = (XSSFCellStyle) style;

        xssfStyle.setFillForegroundColor(
                new XSSFColor(hexToBytes(hexRgb), null)
        );

        xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private byte[] hexToBytes(String hex) {

        return new byte[]{
                (byte) Integer.parseInt(hex.substring(0, 2), 16),
                (byte) Integer.parseInt(hex.substring(2, 4), 16),
                (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static class Styles {

        private CellStyle headerPlain;
        private CellStyle headerBold;
        private CellStyle date;
        private CellStyle label;
        private CellStyle montant;
        private CellStyle totalVersementsLabel;
        private CellStyle totalVersementsMontant;
        private CellStyle totalFraisLabel;
        private CellStyle totalFraisMontant;
        private CellStyle grandTotalVersementsLabel;
        private CellStyle grandTotalVersementsMontant;
        private CellStyle grandTotalFraisLabel;
        private CellStyle grandTotalFraisMontant;
        private CellStyle netLabel;
        private CellStyle netMontant;
    }
}