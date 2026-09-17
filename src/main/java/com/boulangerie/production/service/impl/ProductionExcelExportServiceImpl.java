package com.boulangerie.production.service.impl;

import com.boulangerie.production.api.LivreurProductionMensuelleDto;
import com.boulangerie.production.api.ProductionDistributionMensuelleDto;
import com.boulangerie.production.api.ProductionMensuelleReportDto;
import com.boulangerie.production.exception.ExcelExportException;
import com.boulangerie.production.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionExcelExportServiceImpl
        implements ProductionExcelExportService {

    private static final String SHEET_NAME =
            ProductionExcelLayout.SHEET_NAME;

    /*
     * ============================================================
     * COLONNES TECHNIQUES
     * ============================================================
     *
     * Dérivées dynamiquement de layout.totalPpColumn(), et non
     * plus hardcodées : ces valeurs ne coïncidaient avec les
     * vraies colonnes de totaux que pour un mois de 31 jours
     * (décembre). Pour tout autre mois, totalGpColumn/totalPpColumn
     * se décalent vers la gauche mais des constantes fixes
     * resteraient en place, laissant un écart de colonnes
     * inutilisées et non masquées.
     */

    private int hiddenIdColumn(ProductionExcelLayout layout) {
        return layout.totalPpColumn() + 1;
    }

    private int hiddenTypeColumn(ProductionExcelLayout layout) {
        return layout.totalPpColumn() + 2;
    }

    /*
     * ============================================================
     * LIGNES D'EN-TÊTE
     * ============================================================
     */

    private static final int HEADER_DATE_ROW = 0;
    private static final int HEADER_TYPE_ROW = 1;

    /*
     * ============================================================
     * TYPES DE LIGNES EXCEL
     * ============================================================
     */

    private static final String ROW_TYPE_LIVREUR = "LIVREUR";
    private static final String ROW_TYPE_BOUTIQUE = "BOUTIQUE";
    private static final String ROW_TYPE_RATIONS = "RATIONS";
    private static final String ROW_TYPE_AUMONE = "AUMONE";
    private static final String ROW_TYPE_PAIN_GATE_FRAIS =
            "PAIN_GATE_FRAIS";

    /*
     * Format de date du fichier de référence : "mm-dd-yy" (avec
     * année, sur 2 chiffres). L'export précédent utilisait "dd/MM"
     * (sans année du tout), ce qui devenait ambigu dès qu'on
     * compare des rapports de mois/années différents.
     *
     * NOTE : "mm-dd-yy" est un format anglo-saxon (mois/jour/année).
     * Si un format jour/mois/année est préféré pour une entreprise
     * francophone, remplacer par "dd/MM/yyyy" ou "dd/MM/yy".
     */
    private static final String DATE_FORMAT = "dd-mm-yyyy";



    private static final String COLOR_DATE_LABEL = "FFF2CC";   // Accent4, Lighter 80%
    private static final String COLOR_ORANGE = "ED7D31";       // Accent2
    private static final String COLOR_GOLD = "FFD966";         // Accent4, Lighter 40%
    private static final String COLOR_BLUE = "00B0F0";         // Standard Blue
    private static final String COLOR_GRAY = "C9C9C9";         // Accent3, Lighter 40%
    private static final String COLOR_GRAY_TOTAL = "D0CFCF";   // Background2, Darker 10%
    private static final String COLOR_GREEN = "92D050";        // Standard Green
    private static final String COLOR_YELLOW = "FFFF00";       // Standard Yellow
    private static final String COLOR_WHITE = "FFFFFF";        // Background1
    private static final String COLOR_LEVURE_LABEL = "9DC3E6"; // Accent1, Lighter 40%
    private static final String COLOR_LEVURE_TOTAL = "8F98A6"; // Text2, Lighter 40%

    @Override
    public byte[] exporterProductionMensuelle(
            ProductionMensuelleReportDto report
    ) {

        Objects.requireNonNull(
                report,
                "Le rapport mensuel est obligatoire."
        );

        Objects.requireNonNull(
                report.getPeriode(),
                "La période est obligatoire."
        );

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            Styles styles = createStyles(workbook);

            ProductionExcelLayout layout =
                    ProductionExcelLayout.forPeriod(
                            report.getPeriode()
                    );

            /*
             * ----------------------------------------------------
             * 1. EN-TÊTES
             * ----------------------------------------------------
             */
            ecrireEntetes(
                    sheet,
                    layout,
                    styles
            );

            /*
             * ----------------------------------------------------
             * 2. LIVREURS
             *
             * Le nombre de livreurs est dynamique.
             * ----------------------------------------------------
             */
            int currentRow = layout.firstDataRow();

            for (LivreurProductionMensuelleDto livreur
                    : safeList(report.getLivreurs())) {

                ecrireLivreur(
                        sheet,
                        currentRow,
                        layout,
                        livreur,
                        styles
                );

                currentRow++;
            }

            /*
             * ----------------------------------------------------
             * 3. BOUTIQUE
             *
             * Stylée comme une ligne de PRODUCTION (doré), pas
             * comme une distribution (gris) — conforme au fichier
             * de référence, et cohérent avec le fait qu'elle entre
             * dans le calcul de "Quantité totale en kilo/en Ppain".
             *
             * On mémorise la dernière ligne "production" ici, avant
             * d'écrire les lignes de distribution.
             * ----------------------------------------------------
             */
            currentRow = ecrireDestination(
                    sheet,
                    currentRow,
                    layout,
                    ProductionExcelLabels.BOUTIQUE,
                    ROW_TYPE_BOUTIQUE,
                    report.getBoutique(),
                    styles.labelBlue,
                    styles.productionFill,
                    styles.productionFill,
                    styles
            );

            int lastProductionRow = currentRow - 1; // ligne Boutique
            int firstDistributionRow = currentRow;  // ligne Rations

            /*
             * ----------------------------------------------------
             * 4. RATIONS PERSONNELLES
             *
             * Valeur unique par jour (pas de vraie distinction
             * GP/PP) : les deux colonnes sont fusionnées, comme
             * dans le fichier de référence.
             * ----------------------------------------------------
             */
            currentRow = ecrireDestinationFusionnee(
                    sheet,
                    currentRow,
                    layout,
                    ProductionExcelLabels.RATIONS,
                    ROW_TYPE_RATIONS,
                    report.getRations(),
                    styles.labelBlue,
                    styles.distributionData,
                    styles.distributionTotal
            );

            /*
             * ----------------------------------------------------
             * 5. AUMÔNE
             * ----------------------------------------------------
             */
            currentRow = ecrireDestinationFusionnee(
                    sheet,
                    currentRow,
                    layout,
                    ProductionExcelLabels.AUMONE,
                    ROW_TYPE_AUMONE,
                    report.getAumone(),
                    styles.labelBlue,
                    styles.distributionData,
                    styles.distributionTotal
            );

            /*
             * ----------------------------------------------------
             * 6. PAIN GÂTÉ / PAIN FRAIS
             *
             * Ce n'est PAS un produit.
             * Il s'agit d'une quantité de GP/PP identifiée
             * comme pain gâté / pain frais.
             * ----------------------------------------------------
             */
            currentRow = ecrireDestinationFusionnee(
                    sheet,
                    currentRow,
                    layout,
                    ProductionExcelLabels.PAIN_GATE_FRAIS,
                    ROW_TYPE_PAIN_GATE_FRAIS,
                    report.getPainGatePainFrais(),
                    styles.labelBlue,
                    styles.distributionData,
                    styles.distributionTotal
            );

            int lastDistributionRow = currentRow - 1; // ligne Pain gâté/frais

            /*
             * Boulanger et Pain mangé sont volontairement exclus.
             */

            /*
             * ----------------------------------------------------
             * 7. LIGNES DE CALCUL
             *
             * Leur position dépend du nombre de livreurs.
             * ----------------------------------------------------
             */
            ecrireLignesCalcul(
                    sheet,
                    currentRow,
                    lastProductionRow,
                    firstDistributionRow,
                    lastDistributionRow,
                    layout,
                    report,
                    styles
            );

            /*
             * ----------------------------------------------------
             * 8. CONFIGURATION
             * ----------------------------------------------------
             */
            configurerFeuille(
                    sheet,
                    layout
            );

            return toBytes(workbook);

        } catch (IOException e) {

            throw new ExcelExportException(
                    "Erreur lors de la génération du fichier "
                            + "Excel de production.",
                    e
            );
        }
    }

    /*
     * ============================================================
     * EN-TÊTES
     * ============================================================
     */

    private void ecrireEntetes(
            Sheet sheet,
            ProductionExcelLayout layout,
            Styles styles
    ) {

        Row dateRow =
                sheet.createRow(HEADER_DATE_ROW);

        Row typeRow =
                sheet.createRow(HEADER_TYPE_ROW);

        /*
         * Colonne A.
         */
        Cell dateLabel =
                dateRow.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        dateLabel.setCellValue("Date");
        dateLabel.setCellStyle(styles.dateLabel);

        Cell nameLabel =
                typeRow.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        nameLabel.setCellValue(
                ProductionExcelLabels.NOM
        );

        nameLabel.setCellStyle(styles.labelBlue);

        /*
         * Colonnes journalières.
         *
         * B/C = jour 1 GP/PP
         * D/E = jour 2 GP/PP
         * ...
         */
        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            LocalDate date =
                    layout.periode().atDay(jour);

            int gpColumn =
                    layout.gpColumn(jour);

            int ppColumn =
                    layout.ppColumn(jour);

            /*
             * Date.
             */
            Cell gpDate =
                    dateRow.createCell(gpColumn);

            gpDate.setCellValue(
                    java.sql.Date.valueOf(date)
            );

            gpDate.setCellStyle(
                    styles.dateData
            );

            Cell ppDate =
                    dateRow.createCell(ppColumn);

            ppDate.setCellStyle(
                    styles.dateData
            );

            /*
             * Fusion GP/PP.
             */
            sheet.addMergedRegion(
                    new CellRangeAddress(
                            HEADER_DATE_ROW,
                            HEADER_DATE_ROW,
                            gpColumn,
                            ppColumn
                    )
            );

            /*
             * Types GP / PP.
             */
            Cell gpType =
                    typeRow.createCell(gpColumn);

            gpType.setCellValue("GP");
            gpType.setCellStyle(styles.typeHeader);

            Cell ppType =
                    typeRow.createCell(ppColumn);

            ppType.setCellValue("PP");
            ppType.setCellStyle(styles.typeHeader);
        }

        /*
         * Totaux.
         *
         * Dans le fichier de référence, la ligne de date (ligne 1)
         * porte les libellés "Total kilo" / "Total P pain", et la
         * ligne de type (ligne 2) porte "Gpain" / "Ppain". Les deux
         * lignes NE sont PAS fusionnées pour ces colonnes. Les 4
         * cellules sont en jaune plein.
         */
        Cell totalKiloHeader =
                dateRow.createCell(
                        layout.totalGpColumn()
                );

        totalKiloHeader.setCellValue("Total kilo");
        totalKiloHeader.setCellStyle(styles.totalHeader);

        Cell totalPpainHeader =
                dateRow.createCell(
                        layout.totalPpColumn()
                );

        totalPpainHeader.setCellValue("Total P pain");
        totalPpainHeader.setCellStyle(styles.totalHeader);

        Cell totalGpType =
                typeRow.createCell(
                        layout.totalGpColumn()
                );

        totalGpType.setCellValue("Gpain");
        totalGpType.setCellStyle(styles.totalHeader);

        Cell totalPpType =
                typeRow.createCell(
                        layout.totalPpColumn()
                );

        totalPpType.setCellValue("Ppain");
        totalPpType.setCellStyle(styles.totalHeader);
    }

    /*
     * ============================================================
     * LIVREUR
     * ============================================================
     */

    private void ecrireLivreur(
            Sheet sheet,
            int rowIndex,
            ProductionExcelLayout layout,
            LivreurProductionMensuelleDto livreur,
            Styles styles
    ) {

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        /*
         * Nom.
         */
        Cell label =
                row.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        label.setCellValue(
                nullSafe(livreur.getNom())
        );

        label.setCellStyle(styles.labelBlue);

        /*
         * Identité cachée.
         */
        ecrireMetadata(
                row,
                layout,
                livreur.getLivreurId(),
                ROW_TYPE_LIVREUR
        );

        /*
         * Quantités journalières.
         */
        ecrireQuantites(
                row,
                layout,
                livreur.getQuantitesGp(),
                livreur.getQuantitesPp(),
                styles.productionFill
        );

        /*
         * Totaux mensuels.
         */
        ecrireTotaux(
                row,
                layout,
                livreur.getQuantiteGpTotale(),
                livreur.getQuantitePpTotale(),
                styles.productionFill
        );
    }

    /*
     * ============================================================
     * DESTINATIONS
     * ============================================================
     */

    private int ecrireDestination(
            Sheet sheet,
            int rowIndex,
            ProductionExcelLayout layout,
            String label,
            String rowType,
            ProductionDistributionMensuelleDto destination,
            CellStyle labelStyle,
            CellStyle dataStyle,
            CellStyle totalStyle,
            Styles styles
    ) {

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        Cell labelCell =
                row.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        /*
         * Les destinations fixes n'ont pas d'identifiant
         * métier à conserver.
         */
        ecrireMetadata(
                row,
                layout,
                null,
                rowType
        );

        if (destination != null) {

            ecrireQuantites(
                    row,
                    layout,
                    destination.getQuantitesGp(),
                    destination.getQuantitesPp(),
                    dataStyle
            );

            ecrireTotaux(
                    row,
                    layout,
                    destination.getQuantiteGpTotale(),
                    destination.getQuantitePpTotale(),
                    totalStyle
            );
        }

        return rowIndex + 1;
    }

    /*
     * ============================================================
     * DESTINATIONS À VALEUR UNIQUE (Rations, Aumône,
     * Pain gâté/Pain frais)
     * ============================================================
     *
     * Contrairement à Boutique (une vraie ligne de production avec
     * GP et PP distincts), ces destinations n'ont qu'UNE quantité
     * par jour. Les colonnes GP/PP du jour sont donc fusionnées et
     * portent la somme GP+PP, exactement comme le total mensuel
     * (une seule cellule fusionnée BL:BM) dans le fichier de
     * référence.
     */
    private int ecrireDestinationFusionnee(
            Sheet sheet,
            int rowIndex,
            ProductionExcelLayout layout,
            String label,
            String rowType,
            ProductionDistributionMensuelleDto destination,
            CellStyle labelStyle,
            CellStyle dataStyle,
            CellStyle totalStyle
    ) {

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        Cell labelCell =
                row.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        ecrireMetadata(
                row,
                layout,
                null,
                rowType
        );

        if (destination != null) {

            for (int jour = 1;
                 jour <= layout.periode().lengthOfMonth();
                 jour++) {

                LocalDate date =
                        layout.periode().atDay(jour);

                BigDecimal combinee =
                        getQuantite(destination.getQuantitesGp(), date)
                                .add(
                                        getQuantite(
                                                destination.getQuantitesPp(),
                                                date
                                        )
                                );

                int gpColumn = layout.gpColumn(jour);
                int ppColumn = layout.ppColumn(jour);

                Cell cell = row.createCell(gpColumn);
                cell.setCellValue(combinee.doubleValue());
                cell.setCellStyle(dataStyle);

                mergeRowColumns(
                        sheet,
                        rowIndex,
                        gpColumn,
                        ppColumn,
                        dataStyle
                );
            }

            BigDecimal totalCombine =
                    safe(destination.getQuantiteGpTotale())
                            .add(safe(destination.getQuantitePpTotale()));

            Cell totalCell =
                    row.createCell(layout.totalGpColumn());

            totalCell.setCellValue(totalCombine.doubleValue());
            totalCell.setCellStyle(totalStyle);

            mergeRowColumns(
                    sheet,
                    rowIndex,
                    layout.totalGpColumn(),
                    layout.totalPpColumn(),
                    totalStyle
            );
        }

        return rowIndex + 1;
    }

    /*
     * Fusionne deux colonnes adjacentes sur une même ligne (ex :
     * une paire GP/PP journalière, ou les deux colonnes de total
     * mensuel) et applique le même style aux deux cellules pour
     * que la bordure s'affiche correctement sur toute la cellule
     * fusionnée.
     */
    private void mergeRowColumns(
            Sheet sheet,
            int rowIndex,
            int startColumn,
            int endColumn,
            CellStyle style
    ) {

        Row row = getOrCreateRow(sheet, rowIndex);

        Cell trailingCell = row.createCell(endColumn);
        trailingCell.setCellStyle(style);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        startColumn,
                        endColumn
                )
        );
    }

    /*
     * ============================================================
     * MÉTADONNÉES CACHÉES
     * ============================================================
     */

    private void ecrireMetadata(
            Row row,
            ProductionExcelLayout layout,
            Long entityId,
            String rowType
    ) {

        Cell idCell =
                row.createCell(
                        hiddenIdColumn(layout)
                );

        idCell.setCellValue(
                entityId == null
                        ? 0
                        : entityId
        );

        Cell typeCell =
                row.createCell(
                        hiddenTypeColumn(layout)
                );

        typeCell.setCellValue(rowType);
    }

    /*
     * ============================================================
     * QUANTITÉS JOURNALIÈRES
     * ============================================================
     */

    private void ecrireQuantites(
            Row row,
            ProductionExcelLayout layout,
            Map<LocalDate, BigDecimal> quantitesGp,
            Map<LocalDate, BigDecimal> quantitesPp,
            CellStyle dataStyle
    ) {

        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            LocalDate date =
                    layout.periode().atDay(jour);

            BigDecimal gp =
                    getQuantite(
                            quantitesGp,
                            date
                    );

            BigDecimal pp =
                    getQuantite(
                            quantitesPp,
                            date
                    );

            /*
             * GP.
             */
            Cell gpCell =
                    row.createCell(
                            layout.gpColumn(jour)
                    );

            gpCell.setCellValue(
                    gp.doubleValue()
            );

            gpCell.setCellStyle(
                    dataStyle
            );

            /*
             * PP.
             */
            Cell ppCell =
                    row.createCell(
                            layout.ppColumn(jour)
                    );

            ppCell.setCellValue(
                    pp.doubleValue()
            );

            ppCell.setCellStyle(
                    dataStyle
            );
        }
    }

    /*
     * ============================================================
     * TOTAUX
     * ============================================================
     */

    private void ecrireTotaux(
            Row row,
            ProductionExcelLayout layout,
            BigDecimal totalGp,
            BigDecimal totalPp,
            CellStyle totalStyle
    ) {

        Cell gp =
                row.createCell(
                        layout.totalGpColumn()
                );

        gp.setCellValue(
                safe(totalGp).doubleValue()
        );

        gp.setCellStyle(
                totalStyle
        );

        Cell pp =
                row.createCell(
                        layout.totalPpColumn()
                );

        pp.setCellValue(
                safe(totalPp).doubleValue()
        );

        pp.setCellStyle(
                totalStyle
        );
    }

    /*
     * ============================================================
     * LIGNES DE CALCUL
     * ============================================================
     */

    private void ecrireLignesCalcul(
            Sheet sheet,
            int firstCalculationRow,
            int lastProductionRow,
            int firstDistributionRow,
            int lastDistributionRow,
            ProductionExcelLayout layout,
            ProductionMensuelleReportDto report,
            Styles styles
    ) {

        int totalProductionRow = firstCalculationRow;
        int totalKiloRow = totalProductionRow + 1;
        int sacsFarineRow = totalProductionRow + 2;
        int rendementSouhaiteRow = totalProductionRow + 3;
        int rendementObtenuRow = totalProductionRow + 4;
        int differenceRow = totalProductionRow + 5;
        int levureRow = totalProductionRow + 6;
        int ameliorantRow = totalProductionRow + 7;

        /*
         * ============================================================
         * LABELS
         * ============================================================
         */

        ecrireLabel(
                sheet,
                totalProductionRow,
                ProductionExcelLabels.TOTAL_KILO_PP,
                styles.productionFill
        );

        ecrireLabel(
                sheet,
                totalKiloRow,
                ProductionExcelLabels.TOTAL_KILO,
                styles.totalKiloLabel
        );

        ecrireLabel(
                sheet,
                sacsFarineRow,
                ProductionExcelLabels.SACS_FARINE,
                styles.sacsLabel
        );

        ecrireLabel(
                sheet,
                rendementSouhaiteRow,
                ProductionExcelLabels.RENDEMENT_SOUHAITE,
                styles.sacsLabel
        );

        ecrireLabel(
                sheet,
                rendementObtenuRow,
                ProductionExcelLabels.RENDEMENT_OBTENU,
                styles.labelBlue
        );

        ecrireLabel(
                sheet,
                differenceRow,
                ProductionExcelLabels.DIFFERENCE,
                styles.differenceLabel
        );

        ecrireLabel(
                sheet,
                levureRow,
                ProductionExcelLabels.LEVURE,
                styles.levureLabel
        );

        ecrireLabel(
                sheet,
                ameliorantRow,
                ProductionExcelLabels.AMELIORANT,
                styles.levureLabel
        );

        /*
         * ============================================================
         * DONNÉES JOURNALIÈRES
         * ============================================================
         */

        ecrireSacsFarine(
                sheet,
                sacsFarineRow,
                layout,
                report,
                styles.sacsData
        );

        ecrireConsommableParJour(
                sheet,
                levureRow,
                layout,
                report.getLevureParJour(),
                styles.calculationNoFill
        );

        ecrireConsommableParJour(
                sheet,
                ameliorantRow,
                layout,
                report.getAmeliorantParJour(),
                styles.calculationNoFill
        );

        /*
         * ============================================================
         * CALCULS JOURNALIERS
         * ============================================================
         */

        /*
         * Bornes Excel (1-based) des blocs utilisés par les formules,
         * calquées sur le fichier de référence :
         *
         *  - "Quantité totale en kilo/en Ppain" (totalProductionRow) :
         *        SUM(Livreurs..Boutique)               ex: SUM(B3:B6)
         *
         *  - "Quantité totale en kilo" (totalKiloRow), colonne GP :
         *        totalProductionRow + SUM(Rations..PainGateFrais,
         *        colonnes GP et PP)                     ex: SUM(B12,B7:C11)
         */
        int firstOperationalExcelRow =
                layout.firstDataRow() + 1;

        int lastProductionExcelRow =
                lastProductionRow + 1;

        int firstDistributionExcelRow =
                firstDistributionRow + 1;

        int lastDistributionExcelRow =
                lastDistributionRow + 1;

        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            int gpColumn = layout.gpColumn(jour);
            int ppColumn = layout.ppColumn(jour);

            /*
             * --------------------------------------------------------
             * Quantité totale en kilo/en Ppain
             *
             * On additionne UNIQUEMENT les lignes de production :
             *
             * Livreurs
             * Boutique
             *
             * Les destinations (Rations, Aumône, Pain gâté/frais) ne
             * sont PAS des lignes de production et ne sont pas
             * comptées ici, exactement comme dans le fichier de
             * référence (=SUM(B3:B6)).
             * --------------------------------------------------------
             */

            setFormula(
                    sheet,
                    totalProductionRow,
                    gpColumn,
                    sumFormula(
                            gpColumn,
                            firstOperationalExcelRow,
                            lastProductionExcelRow
                    ),
                    styles.productionFill
            );

            setFormula(
                    sheet,
                    totalProductionRow,
                    ppColumn,
                    sumFormula(
                            ppColumn,
                            firstOperationalExcelRow,
                            lastProductionExcelRow
                    ),
                    styles.productionFill
            );

            /*
             * --------------------------------------------------------
             * Quantité totale en kilo
             *
             * GP = totalProduction(GP) + toutes les quantités
             *      (GP et PP) des lignes de distribution
             *      (Rations, Aumône, Pain gâté/frais)
             *      ex: =SUM(B12,B7:C11)
             *
             * PP = totalProduction(PP) / 2
             *      ex: =C12/2
             *
             * Les deux cellules sont SANS remplissage dans le
             * fichier de référence (contrairement à leur libellé,
             * qui lui est vert).
             * --------------------------------------------------------
             */

            String totalKiloGpFormula =
                    "SUM("
                            + cellReference(
                            gpColumn,
                            totalProductionRow
                    )
                            + ","
                            + columnLetter(gpColumn)
                            + firstDistributionExcelRow
                            + ":"
                            + columnLetter(ppColumn)
                            + lastDistributionExcelRow
                            + ")";

            setFormula(
                    sheet,
                    totalKiloRow,
                    gpColumn,
                    totalKiloGpFormula,
                    styles.calculationNoFill
            );

            setFormula(
                    sheet,
                    totalKiloRow,
                    ppColumn,
                    cellReference(
                            ppColumn,
                            totalProductionRow
                    ) + "/2",
                    styles.calculationNoFill
            );

            /*
             * --------------------------------------------------------
             * Rendement souhaité
             *
             * = quantité prévue du jour, déjà calculée dans le
             * domaine lot par lot via
             * Recette.calculerQuantitePrevue(multiplicateur), et
             * agrégée par jour dans
             * report.getQuantitesPrevuesParJour(). PAS une formule
             * "sacs * 340" : le rendement est une propriété de la
             * recette, pas une constante universelle.
             * --------------------------------------------------------
             */

            setNumericValue(
                    sheet,
                    rendementSouhaiteRow,
                    gpColumn,
                    getRendementSouhaitePourJour(
                            report,
                            layout.periode().atDay(jour)
                    ),
                    styles.calculationNoFill
            );

            mergeRowColumns(
                    sheet,
                    rendementSouhaiteRow,
                    gpColumn,
                    ppColumn,
                    styles.calculationNoFill
            );

            /*
             * --------------------------------------------------------
             * Rendement obtenu
             *
             * GP équivalent + PP équivalent.
             * --------------------------------------------------------
             */

            setFormula(
                    sheet,
                    rendementObtenuRow,
                    gpColumn,
                    cellReference(
                            gpColumn,
                            totalKiloRow
                    )
                            + "+"
                            + cellReference(
                            ppColumn,
                            totalKiloRow
                    ),
                    styles.calculationNoFill
            );

            mergeRowColumns(
                    sheet,
                    rendementObtenuRow,
                    gpColumn,
                    ppColumn,
                    styles.calculationNoFill
            );

            /*
             * --------------------------------------------------------
             * Difference / Surplus
             *
             * Rendement obtenu - rendement souhaité.
             * --------------------------------------------------------
             */

            setFormula(
                    sheet,
                    differenceRow,
                    gpColumn,
                    cellReference(
                            gpColumn,
                            rendementObtenuRow
                    )
                            + "-"
                            + cellReference(
                            gpColumn,
                            rendementSouhaiteRow
                    ),
                    styles.calculationNoFill
            );

            mergeRowColumns(
                    sheet,
                    differenceRow,
                    gpColumn,
                    ppColumn,
                    styles.calculationNoFill
            );
        }

        /*
         * ============================================================
         * TOTAUX MENSUELS DES LIGNES DE CALCUL (colonne BL)
         * ============================================================
         *
         * Dans le fichier de référence, chaque ligne de calcul (sauf
         * "Quantité totale en kilo/en Ppain", qui n'en a pas) porte
         * un total mensuel unique en colonne BL — une simple somme
         * sur toute la plage de jours, sans distinction GP/PP, et
         * SANS fusion BL:BM (contrairement aux lignes de
         * distribution). BM reste vide.
         *
         * Ce bloc était entièrement absent de l'export précédent.
         */

        setFormula(
                sheet,
                totalKiloRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        totalKiloRow
                ),
                styles.totalKiloLabel
        );

        setFormula(
                sheet,
                sacsFarineRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        sacsFarineRow
                ),
                styles.sacsData
        );

        mergeRowColumns(
                sheet,
                sacsFarineRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.sacsData
        );

        setFormula(
                sheet,
                rendementSouhaiteRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        rendementSouhaiteRow
                ),
                styles.sacsLabel
        );

        mergeRowColumns(
                sheet,
                rendementSouhaiteRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.sacsLabel
        );

        setFormula(
                sheet,
                rendementObtenuRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        rendementObtenuRow
                ),
                styles.sacsLabel
        );

        mergeRowColumns(
                sheet,
                rendementObtenuRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.sacsLabel
        );

        setFormula(
                sheet,
                differenceRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        differenceRow
                ),
                styles.sacsLabel
        );

        mergeRowColumns(
                sheet,
                differenceRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.sacsLabel
        );

        setFormula(
                sheet,
                levureRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        levureRow
                ),
                styles.levureTotal
        );

        mergeRowColumns(
                sheet,
                levureRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.levureTotal
        );

        setFormula(
                sheet,
                ameliorantRow,
                layout.totalGpColumn(),
                rowRangeSumFormula(
                        layout.firstDayColumn(),
                        layout.lastDayColumn(),
                        ameliorantRow
                ),
                styles.levureTotal
        );

        mergeRowColumns(
                sheet,
                ameliorantRow,
                layout.totalGpColumn(),
                layout.totalPpColumn(),
                styles.levureTotal
        );
    }

    private void ecrireConsommableParJour(
            Sheet sheet,
            int rowIndex,
            ProductionExcelLayout layout,
            Map<LocalDate, BigDecimal> quantitesParJour,
            CellStyle dataStyle
    ) {

        Row row = getOrCreateRow(sheet, rowIndex);

        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            LocalDate date =
                    layout.periode().atDay(jour);

            BigDecimal quantite =
                    getQuantite(
                            quantitesParJour,
                            date
                    );

            Cell cell =
                    row.createCell(
                            layout.gpColumn(jour)
                    );

            cell.setCellValue(
                    quantite.doubleValue()
            );

            cell.setCellStyle(dataStyle);

            mergeRowColumns(
                    sheet,
                    rowIndex,
                    layout.gpColumn(jour),
                    layout.ppColumn(jour),
                    dataStyle
            );
        }
    }

    private String sumFormula(
            int column,
            int firstRow,
            int lastRow
    ) {
        return "SUM("
                + columnLetter(column)
                + firstRow
                + ":"
                + columnLetter(column)
                + lastRow
                + ")";
    }

    /*
     * Somme horizontale sur une seule ligne, entre deux colonnes,
     * ex: SUM(B13:BK13). Utilisée pour les totaux mensuels des
     * lignes de calcul.
     */
    private String rowRangeSumFormula(
            int startColumn,
            int endColumn,
            int excelRow
    ) {
        return "SUM("
                + columnLetter(startColumn)
                + excelRow
                + ":"
                + columnLetter(endColumn)
                + excelRow
                + ")";
    }

    private String cellReference(
            int column,
            int zeroBasedRow
    ) {
        return columnLetter(column)
                + (zeroBasedRow + 1);
    }

    /*
     * ============================================================
     * RENDEMENT SOUHAITÉ
     * ============================================================
     */

    private BigDecimal getRendementSouhaitePourJour(
            ProductionMensuelleReportDto report,
            LocalDate date
    ) {
        if (report.getQuantitesPrevuesParJour() == null) {
            return BigDecimal.ZERO;
        }

        return safe(
                report.getQuantitesPrevuesParJour().get(date)
        );
    }

    private void setNumericValue(
            Sheet sheet,
            int rowIndex,
            int columnIndex,
            BigDecimal value,
            CellStyle style
    ) {
        Cell cell =
                getOrCreateRow(
                        sheet,
                        rowIndex
                ).createCell(columnIndex);

        cell.setCellValue(
                safe(value).doubleValue()
        );

        cell.setCellStyle(style);
    }

    private void setFormula(
            Sheet sheet,
            int rowIndex,
            int columnIndex,
            String formula,
            CellStyle style
    ) {

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        Cell cell =
                row.createCell(
                        columnIndex
                );

        cell.setCellFormula(formula);
        cell.setCellStyle(style);
    }

    /*
     * ============================================================
     * SACS DE FARINE
     * ============================================================
     */

    private void ecrireSacsFarine(
            Sheet sheet,
            int rowIndex,
            ProductionExcelLayout layout,
            ProductionMensuelleReportDto report,
            CellStyle dataStyle
    ) {

        Map<LocalDate, BigDecimal> sacsParJour =
                report.getSacsFarineParJour();

        if (sacsParJour == null) {
            return;
        }

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            LocalDate date =
                    layout.periode().atDay(jour);

            BigDecimal sacs =
                    safe(
                            sacsParJour.get(date)
                    );

            Cell cell =
                    row.createCell(
                            layout.gpColumn(jour)
                    );

            cell.setCellValue(
                    sacs.doubleValue()
            );

            cell.setCellStyle(
                    dataStyle
            );

            mergeRowColumns(
                    sheet,
                    rowIndex,
                    layout.gpColumn(jour),
                    layout.ppColumn(jour),
                    dataStyle
            );
        }
    }

    /*
     * ============================================================
     * HELPERS
     * ============================================================
     */

    private void ecrireLabel(
            Sheet sheet,
            int rowIndex,
            String value,
            CellStyle style
    ) {

        Row row =
                getOrCreateRow(
                        sheet,
                        rowIndex
                );

        Cell cell =
                row.createCell(
                        ProductionExcelLayout.LABEL_COLUMN
                );

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private Row getOrCreateRow(
            Sheet sheet,
            int rowIndex
    ) {

        Row row =
                sheet.getRow(rowIndex);

        return row != null
                ? row
                : sheet.createRow(rowIndex);
    }

    private BigDecimal getQuantite(
            Map<LocalDate, BigDecimal> quantites,
            LocalDate date
    ) {

        if (quantites == null) {
            return BigDecimal.ZERO;
        }

        return safe(
                quantites.get(date)
        );
    }

    private BigDecimal safe(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private String nullSafe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private List<LivreurProductionMensuelleDto> safeList(
            List<LivreurProductionMensuelleDto> livreurs
    ) {

        return livreurs == null
                ? Collections.emptyList()
                : livreurs;
    }

    /*
     * ============================================================
     * COLUMN LETTER
     * ============================================================
     */

    private String columnLetter(
            int zeroBasedColumn
    ) {

        int column =
                zeroBasedColumn + 1;

        StringBuilder result =
                new StringBuilder();

        while (column > 0) {

            int remainder =
                    (column - 1) % 26;

            result.insert(
                    0,
                    (char) ('A' + remainder)
            );

            column =
                    (column - 1) / 26;
        }

        return result.toString();
    }

    /*
     * ============================================================
     * CONFIGURATION FEUILLE
     * ============================================================
     */

    private void configurerFeuille(
            Sheet sheet,
            ProductionExcelLayout layout
    ) {

        /*
         * Bloque :
         * - colonne A
         * - la ligne de dates uniquement (ligne 1)
         *
         * Le fichier de référence fige xSplit=1 / ySplit=1
         * (et non 2 lignes).
         */
        sheet.createFreezePane(
                1,
                1
        );

        sheet.setDefaultRowHeightInPoints(20);

        sheet.getRow(
                HEADER_DATE_ROW
        ).setHeightInPoints(25);

        sheet.getRow(
                HEADER_TYPE_ROW
        ).setHeightInPoints(22);

        /*
         * Nom.
         */
        sheet.setColumnWidth(
                ProductionExcelLayout.LABEL_COLUMN,
                28 * 256
        );

        /*
         * GP / PP journaliers.
         */
        for (int jour = 1;
             jour <= layout.periode().lengthOfMonth();
             jour++) {

            sheet.setColumnWidth(
                    layout.gpColumn(jour),
                    10 * 256
            );

            sheet.setColumnWidth(
                    layout.ppColumn(jour),
                    10 * 256
            );
        }

        /*
         * Totaux GP / PP.
         */
        sheet.setColumnWidth(
                layout.totalGpColumn(),
                13 * 256
        );

        sheet.setColumnWidth(
                layout.totalPpColumn(),
                13 * 256
        );

        /*
         * Colonnes techniques.
         */
        sheet.setColumnHidden(
                hiddenIdColumn(layout),
                true
        );

        sheet.setColumnHidden(
                hiddenTypeColumn(layout),
                true
        );

        /*
         * Recalcul des formules à l'ouverture.
         */
        sheet.setForceFormulaRecalculation(true);
    }

    /*
     * ============================================================
     * STYLES
     * ============================================================
     *
     * Toutes les polices sont non-grasses (bold=false) : le fichier
     * de référence n'utilise JAMAIS le gras, uniquement des
     * remplissages de couleur pour la hiérarchie visuelle.
     * ============================================================
     */

    private Styles createStyles(
            Workbook workbook
    ) {

        Styles styles = new Styles();

        // En-tête "Date" (A1).
        styles.dateLabel =
                baseStyle(workbook, HorizontalAlignment.LEFT);
        fill(styles.dateLabel, COLOR_DATE_LABEL);

        // Cellules de date journalières (ligne 1, B..BJ).
        styles.dateData =
                baseStyle(workbook, HorizontalAlignment.CENTER);
        styles.dateData.setDataFormat(
                workbook.createDataFormat().getFormat(DATE_FORMAT)
        );
        fill(styles.dateData, COLOR_ORANGE);

        // "Total kilo" / "Total P pain" / "Gpain" / "Ppain".
        styles.totalHeader =
                baseStyle(workbook, HorizontalAlignment.CENTER);
        fill(styles.totalHeader, COLOR_YELLOW);

        // "Nom", chaque livreur, Boutique/Rations/Aumône/Pain gâté,
        // et le libellé "Rendement obtenu" : tous en bleu.
        styles.labelBlue =
                baseStyle(workbook, HorizontalAlignment.LEFT);
        fill(styles.labelBlue, COLOR_BLUE);

        // "GP"/"PP" (ligne 2).
        styles.typeHeader =
                baseNumberStyle(workbook);
        fill(styles.typeHeader, COLOR_GOLD);

        // Données + totaux Livreurs/Boutique, et libellé + données
        // de "Quantité totale en kilo/en Ppain" (même doré).
        styles.productionFill =
                baseNumberStyle(workbook);
        fill(styles.productionFill, COLOR_GOLD);

        // Données Rations/Aumône/Pain gâté-frais.
        styles.distributionData =
                baseNumberStyle(workbook);
        fill(styles.distributionData, COLOR_GRAY);

        // Totaux mensuels Rations/Aumône/Pain gâté-frais.
        styles.distributionTotal =
                baseNumberStyle(workbook);
        fill(styles.distributionTotal, COLOR_GRAY_TOTAL);

        // Libellé + total mensuel de "Quantité totale en kilo".
        styles.totalKiloLabel =
                baseNumberStyle(workbook);
        fill(styles.totalKiloLabel, COLOR_GREEN);

        // Libellé "Nombre de sacs de farine", et libellé + total
        // mensuel de "Rendement souhaité"/"Rendement obtenu"/
        // "Difference-Surplus" (même orange).
        styles.sacsLabel =
                baseNumberStyle(workbook);
        fill(styles.sacsLabel, COLOR_ORANGE);

        // Données + total mensuel de "Nombre de sacs de farine".
        styles.sacsData =
                baseNumberStyle(workbook);
        fill(styles.sacsData, COLOR_YELLOW);

        // Libellé "Difference/Surplus" (blanc).
        styles.differenceLabel =
                baseNumberStyle(workbook);
        fill(styles.differenceLabel, COLOR_WHITE);

        // Libellés Levure/Améliorant.
        styles.levureLabel =
                baseNumberStyle(workbook);
        fill(styles.levureLabel, COLOR_LEVURE_LABEL);

        // Totaux mensuels Levure/Améliorant.
        styles.levureTotal =
                baseNumberStyle(workbook);
        fill(styles.levureTotal, COLOR_LEVURE_TOTAL);

        // Cellules de calcul journalières SANS remplissage :
        // Quantité totale en kilo, Rendement souhaité, Rendement
        // obtenu, Difference/Surplus, Levure, Améliorant.
        styles.calculationNoFill =
                baseNumberStyle(workbook);

        return styles;
    }

    private CellStyle baseStyle(
            Workbook workbook,
            HorizontalAlignment alignment
    ) {

        CellStyle style = workbook.createCellStyle();

        style.setAlignment(alignment);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        setBorders(style);

        Font font = workbook.createFont();
        font.setBold(false);
        style.setFont(font);

        return style;
    }

    private CellStyle baseNumberStyle(
            Workbook workbook
    ) {

        CellStyle style =
                baseStyle(workbook, HorizontalAlignment.CENTER);

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("#,##0.##")
        );

        return style;
    }

    /*
     * Applique un remplissage plein à une couleur RGB exacte.
     * Nécessite XSSFCellStyle (toujours le cas ici, le classeur
     * étant systématiquement un XSSFWorkbook).
     */
    private void fill(
            CellStyle style,
            String hexRgb
    ) {

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

    private void setBorders(
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

    /*
     * ============================================================
     * BYTES
     * ============================================================
     */

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

    /*
     * ============================================================
     * STYLE HOLDER
     * ============================================================
     */

    private static class Styles {

        private CellStyle dateLabel;
        private CellStyle dateData;
        private CellStyle totalHeader;
        private CellStyle labelBlue;
        private CellStyle typeHeader;
        private CellStyle productionFill;
        private CellStyle distributionData;
        private CellStyle distributionTotal;
        private CellStyle totalKiloLabel;
        private CellStyle sacsLabel;
        private CellStyle sacsData;
        private CellStyle differenceLabel;
        private CellStyle levureLabel;
        private CellStyle levureTotal;
        private CellStyle calculationNoFill;
    }
}