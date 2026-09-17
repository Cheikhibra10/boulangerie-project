package com.boulangerie.production.service.impl;

import com.boulangerie.production.api.LivreurProductionMensuelleDto;
import com.boulangerie.production.api.ProductionDistributionMensuelleDto;
import com.boulangerie.production.api.ProductionMensuelleReportDto;

import com.boulangerie.production.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Export CSV du rapport mensuel de production.
 *
 * Miroir structurel du fichier Excel (mêmes colonnes, même ordre
 * de lignes), mais avec des valeurs LITTÉRALES partout où le
 * fichier Excel a une formule vivante — le CSV n'a pas de moteur
 * de calcul, donc chaque formule Excel est reproduite ici comme du
 * calcul Java. Voir le commentaire sur chaque ligne pour la
 * formule Excel équivalente qu'elle reproduit.
 *
 * Les colonnes GP/PP sont conservées séparément pour les lignes
 * qui ont une vraie distinction GP/PP (livreurs, boutique, les
 * deux lignes "Quantité totale..."). Pour les lignes à valeur
 * unique (Rations, Aumône, Pain gâté/frais, Sacs de farine,
 * Rendement souhaité/obtenu, Difference, Levure, Améliorant) — qui
 * sont fusionnées GP:PP dans le fichier Excel — la colonne PP est
 * laissée VIDE et la valeur combinée est placée dans la colonne GP,
 * pour rester visuellement cohérent avec ce que montre le fichier
 * Excel.
 */
@Service
public class ProductionCsvExportServiceImpl
        implements ProductionCsvExportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SEPARATOR = ";";

    private static final String LINE_SEPARATOR = "\r\n";

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

        int nbJours = report.getPeriode().lengthOfMonth();

        List<LocalDate> jours = report.getPeriode()
                .atDay(1)
                .datesUntil(report.getPeriode().atEndOfMonth().plusDays(1))
                .toList();

        StringBuilder csv = new StringBuilder();

        /*
         * BOM UTF-8. Permet à Excel de détecter correctement les
         * caractères accentués (Rendement souhaité, Différence,
         * Améliorant, noms de livreurs...).
         */
        csv.append('\uFEFF');

        ecrireEntete(csv, jours);

        for (LivreurProductionMensuelleDto livreur
                : safeList(report.getLivreurs())) {

            ecrireLigneGpPpSepare(
                    csv,
                    nullSafe(livreur.getNom()),
                    jours,
                    livreur.getQuantitesGp(),
                    livreur.getQuantitesPp(),
                    livreur.getQuantiteGpTotale(),
                    livreur.getQuantitePpTotale()
            );
        }

        ecrireLigneGpPpSepare(
                csv,
                ProductionExcelLabels.BOUTIQUE,
                jours,
                mapGp(report.getBoutique()),
                mapPp(report.getBoutique()),
                totalGp(report.getBoutique()),
                totalPp(report.getBoutique())
        );

        /*
         * Valeurs combinées (GP+PP) par jour pour les trois
         * destinations à valeur unique — calculées une fois ici
         * car totalKiloRow en a aussi besoin plus bas.
         */
        BigDecimal[] rationsCombine = combineParJour(report.getRations(), jours);
        BigDecimal[] aumoneCombine = combineParJour(report.getAumone(), jours);
        BigDecimal[] painGateFraisCombine =
                combineParJour(report.getPainGatePainFrais(), jours);

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.RATIONS,
                rationsCombine,
                totalCombine(report.getRations())
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.AUMONE,
                aumoneCombine,
                totalCombine(report.getAumone())
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.PAIN_GATE_FRAIS,
                painGateFraisCombine,
                totalCombine(report.getPainGatePainFrais())
        );

        /*
         * --------------------------------------------------------
         * Quantité totale en kilo/en Ppain
         * Excel : =SUM(Livreurs..Boutique) par colonne GP et PP,
         * séparément. Pas de total mensuel (cf. fichier de
         * référence).
         * --------------------------------------------------------
         */
        BigDecimal[] totalProductionGp = new BigDecimal[nbJours];
        BigDecimal[] totalProductionPp = new BigDecimal[nbJours];

        for (int i = 0; i < nbJours; i++) {

            LocalDate date = jours.get(i);

            BigDecimal sommeGp = get(mapGp(report.getBoutique()), date);
            BigDecimal sommePp = get(mapPp(report.getBoutique()), date);

            for (LivreurProductionMensuelleDto livreur
                    : safeList(report.getLivreurs())) {

                sommeGp = sommeGp.add(
                        get(livreur.getQuantitesGp(), date)
                );

                sommePp = sommePp.add(
                        get(livreur.getQuantitesPp(), date)
                );
            }

            totalProductionGp[i] = sommeGp;
            totalProductionPp[i] = sommePp;
        }

        ecrireLigneGpPpSepareValeurs(
                csv,
                ProductionExcelLabels.TOTAL_KILO_PP,
                totalProductionGp,
                totalProductionPp,
                null,
                null
        );

        /*
         * --------------------------------------------------------
         * Quantité totale en kilo
         * Excel : GP = SUM(totalProductionRow.GP, blocDistribution
         *              GP+PP) ex: =SUM(B12,B7:C11)
         *         PP = totalProductionRow.PP / 2
         * Total mensuel : SOMME COMBINÉE (GP+PP) sur tout le mois,
         * colonne PP du total laissée vide — reproduit
         * =SUM(B13:BK13) du fichier de référence, qui additionne
         * les deux colonnes ensemble pour chaque jour.
         * --------------------------------------------------------
         */
        BigDecimal[] totalKiloGp = new BigDecimal[nbJours];
        BigDecimal[] totalKiloPp = new BigDecimal[nbJours];
        BigDecimal totalKiloMensuelCombine = BigDecimal.ZERO;

        for (int i = 0; i < nbJours; i++) {

            BigDecimal blocDistribution =
                    rationsCombine[i]
                            .add(aumoneCombine[i])
                            .add(painGateFraisCombine[i]);

            totalKiloGp[i] = totalProductionGp[i].add(blocDistribution);
            totalKiloPp[i] = totalProductionPp[i].divide(
                    BigDecimal.valueOf(2)
            );

            totalKiloMensuelCombine = totalKiloMensuelCombine
                    .add(totalKiloGp[i])
                    .add(totalKiloPp[i]);
        }

        ecrireLigneGpPpSepareValeurs(
                csv,
                ProductionExcelLabels.TOTAL_KILO,
                totalKiloGp,
                totalKiloPp,
                totalKiloMensuelCombine,
                null
        );

        /*
         * --------------------------------------------------------
         * Nombre de sacs de farine
         * --------------------------------------------------------
         */
        BigDecimal[] sacsFarine = valeursParJour(
                report.getSacsFarineParJour(),
                jours
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.SACS_FARINE,
                sacsFarine,
                sommer(sacsFarine)
        );

        /*
         * --------------------------------------------------------
         * Rendement souhaité
         * Excel : valeur du domaine (report.quantitesPrevuesParJour),
         * pas une formule "sacs * 340".
         * --------------------------------------------------------
         */
        BigDecimal[] rendementSouhaite = valeursParJour(
                report.getQuantitesPrevuesParJour(),
                jours
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.RENDEMENT_SOUHAITE,
                rendementSouhaite,
                sommer(rendementSouhaite)
        );

        /*
         * --------------------------------------------------------
         * Rendement obtenu = totalKilo.GP + totalKilo.PP (par jour)
         * --------------------------------------------------------
         */
        BigDecimal[] rendementObtenu = new BigDecimal[nbJours];

        for (int i = 0; i < nbJours; i++) {
            rendementObtenu[i] = totalKiloGp[i].add(totalKiloPp[i]);
        }

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.RENDEMENT_OBTENU,
                rendementObtenu,
                sommer(rendementObtenu)
        );

        /*
         * --------------------------------------------------------
         * Difference/Surplus = Rendement obtenu - Rendement souhaité
         * --------------------------------------------------------
         */
        BigDecimal[] difference = new BigDecimal[nbJours];

        for (int i = 0; i < nbJours; i++) {
            difference[i] =
                    rendementObtenu[i].subtract(rendementSouhaite[i]);
        }

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.DIFFERENCE,
                difference,
                sommer(difference)
        );

        /*
         * --------------------------------------------------------
         * Levure / Améliorant
         * --------------------------------------------------------
         */
        BigDecimal[] levure = valeursParJour(
                report.getLevureParJour(),
                jours
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.LEVURE,
                levure,
                sommer(levure)
        );

        BigDecimal[] ameliorant = valeursParJour(
                report.getAmeliorantParJour(),
                jours
        );

        ecrireLigneValeurUnique(
                csv,
                ProductionExcelLabels.AMELIORANT,
                ameliorant,
                sommer(ameliorant)
        );

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /*
     * ============================================================
     * EN-TÊTE
     * ============================================================
     */

    private void ecrireEntete(StringBuilder csv, List<LocalDate> jours) {

        csv.append(csvField("Nom"));

        for (LocalDate jour : jours) {

            String libelle = DATE_FORMAT.format(jour);

            csv.append(SEPARATOR).append(csvField(libelle + " GP"));
            csv.append(SEPARATOR).append(csvField(libelle + " PP"));
        }

        csv.append(SEPARATOR).append(csvField("Gpain"));
        csv.append(SEPARATOR).append(csvField("Ppain"));
        csv.append(LINE_SEPARATOR);
    }

    /*
     * ============================================================
     * LIGNES GP/PP SÉPARÉES (Livreurs, Boutique, deux lignes
     * "Quantité totale...")
     * ============================================================
     */

    private void ecrireLigneGpPpSepare(
            StringBuilder csv,
            String label,
            List<LocalDate> jours,
            Map<LocalDate, BigDecimal> gpParJour,
            Map<LocalDate, BigDecimal> ppParJour,
            BigDecimal totalGp,
            BigDecimal totalPp
    ) {

        csv.append(csvField(label));

        for (LocalDate jour : jours) {

            csv.append(SEPARATOR).append(
                    csvField(get(gpParJour, jour))
            );

            csv.append(SEPARATOR).append(
                    csvField(get(ppParJour, jour))
            );
        }

        csv.append(SEPARATOR).append(csvField(safe(totalGp)));
        csv.append(SEPARATOR).append(csvField(safe(totalPp)));
        csv.append(LINE_SEPARATOR);
    }

    private void ecrireLigneGpPpSepareValeurs(
            StringBuilder csv,
            String label,
            BigDecimal[] gpParJour,
            BigDecimal[] ppParJour,
            BigDecimal totalGp,
            BigDecimal totalPp
    ) {

        csv.append(csvField(label));

        for (int i = 0; i < gpParJour.length; i++) {
            csv.append(SEPARATOR).append(csvField(gpParJour[i]));
            csv.append(SEPARATOR).append(csvField(ppParJour[i]));
        }

        csv.append(SEPARATOR).append(csvField(totalGp));
        csv.append(SEPARATOR).append(csvField(totalPp));
        csv.append(LINE_SEPARATOR);
    }

    /*
     * ============================================================
     * LIGNES À VALEUR UNIQUE (fusionnées GP:PP dans le fichier
     * Excel) : la valeur va dans la colonne GP, la colonne PP est
     * laissée vide pour chaque jour ET pour le total mensuel.
     * ============================================================
     */

    private void ecrireLigneValeurUnique(
            StringBuilder csv,
            String label,
            BigDecimal[] valeursParJour,
            BigDecimal total
    ) {

        csv.append(csvField(label));

        for (BigDecimal valeur : valeursParJour) {
            csv.append(SEPARATOR).append(csvField(valeur));
            csv.append(SEPARATOR); // colonne PP vide
        }

        csv.append(SEPARATOR).append(csvField(total));
        csv.append(SEPARATOR); // colonne PP du total vide
        csv.append(LINE_SEPARATOR);
    }

    /*
     * ============================================================
     * HELPERS DE CALCUL
     * ============================================================
     */

    private BigDecimal[] valeursParJour(
            Map<LocalDate, BigDecimal> map,
            List<LocalDate> jours
    ) {

        BigDecimal[] resultat = new BigDecimal[jours.size()];

        for (int i = 0; i < jours.size(); i++) {
            resultat[i] = get(map, jours.get(i));
        }

        return resultat;
    }

    private BigDecimal[] combineParJour(
            ProductionDistributionMensuelleDto destination,
            List<LocalDate> jours
    ) {

        BigDecimal[] resultat = new BigDecimal[jours.size()];

        for (int i = 0; i < jours.size(); i++) {

            LocalDate date = jours.get(i);

            resultat[i] = get(mapGp(destination), date)
                    .add(get(mapPp(destination), date));
        }

        return resultat;
    }

    private BigDecimal totalCombine(
            ProductionDistributionMensuelleDto destination
    ) {
        return totalGp(destination).add(totalPp(destination));
    }

    private BigDecimal sommer(BigDecimal[] valeurs) {

        BigDecimal somme = BigDecimal.ZERO;

        for (BigDecimal valeur : valeurs) {
            somme = somme.add(valeur);
        }

        return somme;
    }

    private Map<LocalDate, BigDecimal> mapGp(
            ProductionDistributionMensuelleDto destination
    ) {
        return destination == null ? null : destination.getQuantitesGp();
    }

    private Map<LocalDate, BigDecimal> mapPp(
            ProductionDistributionMensuelleDto destination
    ) {
        return destination == null ? null : destination.getQuantitesPp();
    }

    private BigDecimal totalGp(
            ProductionDistributionMensuelleDto destination
    ) {
        return destination == null
                ? BigDecimal.ZERO
                : safe(destination.getQuantiteGpTotale());
    }

    private BigDecimal totalPp(
            ProductionDistributionMensuelleDto destination
    ) {
        return destination == null
                ? BigDecimal.ZERO
                : safe(destination.getQuantitePpTotale());
    }

    private BigDecimal get(
            Map<LocalDate, BigDecimal> map,
            LocalDate date
    ) {

        if (map == null) {
            return BigDecimal.ZERO;
        }

        return safe(map.get(date));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private List<LivreurProductionMensuelleDto> safeList(
            List<LivreurProductionMensuelleDto> livreurs
    ) {
        return livreurs == null ? List.of() : livreurs;
    }

    /*
     * ============================================================
     * CSV : formatage RFC4180 (délimiteur virgule, séparateur
     * décimal point). Les champs contenant une virgule, un
     * guillemet ou un retour à la ligne sont entourés de
     * guillemets, avec les guillemets internes doublés.
     * ============================================================
     */

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