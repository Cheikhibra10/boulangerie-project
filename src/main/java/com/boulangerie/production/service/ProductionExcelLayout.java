package com.boulangerie.production.service;

import java.time.YearMonth;

/**
 * Disposition des colonnes du fichier Excel de production.
 *
 * IMPORTANT : cette classe ne porte QUE ce qui peut être déterminé
 * à partir de la période seule (YearMonth) :
 *
 *  - les colonnes journalières (dépendent du nombre de jours du
 *    mois)
 *  - les colonnes de totaux (Gpain / Ppain)
 *  - la première ligne de données (toujours fixe : juste après les
 *    2 lignes d'en-tête)
 *
 * Les lignes de calcul (Quantité totale, Rendement souhaité,
 * Levure, Améliorant, etc.) NE PEUVENT PAS être connues à partir
 * de la seule période : leur position dépend du nombre de
 * livreurs du rapport, qui est une donnée dynamique connue
 * uniquement à l'exécution (1 livreur, 3 livreurs, 10 livreurs...).
 *
 * Ces lignes étaient auparavant hardcodées ici (11 à 18), ce qui
 * ne correspondait qu'au cas particulier "exactement 3 livreurs" —
 * silencieusement faux pour tout autre mois. Elles ont été
 * retirées ; l'exporteur les calcule désormais lui-même, au moment
 * de l'écriture, à partir du nombre réel de lignes livreurs +
 * destinations effectivement écrites.
 */
public record ProductionExcelLayout(
        YearMonth periode,
        int firstDayColumn,
        int lastDayColumn,
        int totalGpColumn,
        int totalPpColumn,
        int firstDataRow
) {

    public static final String SHEET_NAME = "Feuil1";

    public static final int LABEL_COLUMN = 0;

    /*
     * Excel:
     *
     * A = Nom
     * B/C = 01 GP/PP
     * D/E = 02 GP/PP
     * ...
     * BJ/BK = 31 GP/PP (pour un mois de 31 jours)
     * BL = Gpain
     * BM = Ppain
     */
    public static ProductionExcelLayout forPeriod(YearMonth periode) {

        int days = periode.lengthOfMonth();

        return new ProductionExcelLayout(
                periode,
                1,
                1 + (days * 2) - 1,
                1 + (days * 2),
                1 + (days * 2) + 1,
                2 // first data row (Excel row 3), toujours fixe
        );
    }

    public int gpColumn(int jour) {
        validateDay(jour);

        return firstDayColumn + ((jour - 1) * 2);
    }

    public int ppColumn(int jour) {
        validateDay(jour);

        return gpColumn(jour) + 1;
    }

    private void validateDay(int jour) {

        if (jour < 1 || jour > periode.lengthOfMonth()) {
            throw new IllegalArgumentException(
                    "Jour invalide : " + jour
            );
        }
    }
}