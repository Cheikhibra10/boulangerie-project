package com.boulangerie.production.service.impl;

import com.boulangerie.administration.model.*;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.administration.repository.RecetteIngredientRepository;
import com.boulangerie.administration.repository.RecetteRepository;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.production.api.*;
import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.model.EtatPain;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.repository.DestinationProductionRepository;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.production.service.ProductionReportingService;
import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionReportingServiceImpl
        implements ProductionReportingService {

    private final LotProductionRepository lotProductionRepository;
    private final DestinationProductionRepository destinationRepository;
    private final ProduitService produitService;
    private final LivreurService livreurService;

    /*
     * Pour récupérer directement les RecetteIngredient (levure,
     * améliorant) d'une recette — sans passer par
     * calculerConsommations(multiplicateur) : ces quantités sont
     * des valeurs FIXES par recette, déclarées telles quelles,
     * et ne sont PAS mises à l'échelle par le nombre de sacs de
     * farine utilisés (contrairement à la quantité prévue de
     * pain, qui elle dépend du multiplicateur).
     */
    private final RecetteIngredientRepository recetteIngredientRepository;

    @Override
    public ProductionMensuelleReportDto genererRapportMensuel(
            YearMonth periode
    ) {

        Objects.requireNonNull(
                periode,
                "La période est obligatoire"
        );

        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        /*
         * Productions du mois.
         */
        List<LotProduction> lots =
                lotProductionRepository.findByDateBetweenOrderByDateAsc(
                        debut,
                        fin
                );

        /*
         * Destinations LIVREUR : alimentent la partie
         * "livreurs" du rapport.
         */
        List<DestinationProduction> destinationsLivreur =
                destinationRepository.findByDateBetweenAndCanal(
                        debut,
                        fin,
                        CanalDistribution.LIVREUR
                );

        /*
         * Destinations Boutique / Rations / Aumône : alimentent
         * les lignes de destination fixes du rapport.
         *
         * NOTE : Pain gâté / Pain frais n'est PAS un canal
         * (cf. enum CanalDistribution) — il est identifié via
         * EtatPain au niveau des destinations. Ce mapping reste
         * à faire une fois la structure de DestinationProduction
         * confirmée (cf. TODO dans construirePainGateFrais).
         */
        List<DestinationProduction> destinationsBoutique =
                destinationRepository.findByDateBetweenAndCanal(
                        debut,
                        fin,
                        CanalDistribution.BOUTIQUE
                );

        List<DestinationProduction> destinationsRations =
                destinationRepository.findByDateBetweenAndCanal(
                        debut,
                        fin,
                        CanalDistribution.RATION_PERSONNELLE
                );

        List<DestinationProduction> destinationsAumone =
                destinationRepository.findByDateBetweenAndCanal(
                        debut,
                        fin,
                        CanalDistribution.AUMONE
                );

        /*
         * Pain gâté : du pain devenu impropre à la vente après
         * production. Ce n'est pas un canal — etatPain est un
         * attribut porté par CHAQUE destination, quel que soit
         * son canal (Livreur, Boutique, Rations, Aumône...).
         * On agrège donc sur toute la période sans filtrer par
         * canal, uniquement par état.
         *
         * Nécessite une nouvelle méthode sur
         * DestinationProductionRepository, sur le même principe
         * que findByDateBetweenAndCanal :
         *
         *   List<DestinationProduction> findByDateBetweenAndEtatPain(
         *           LocalDate debut, LocalDate fin, EtatPain etatPain);
         */
        List<DestinationProduction> destinationsPainGate =
                destinationRepository.findByDateBetweenAndEtatPain(
                        debut,
                        fin,
                        EtatPain.GATE
                );

        return construireRapport(
                periode,
                lots,
                destinationsLivreur,
                destinationsBoutique,
                destinationsRations,
                destinationsAumone,
                destinationsPainGate
        );
    }

    private ProductionMensuelleReportDto construireRapport(
            YearMonth periode,
            List<LotProduction> lots,
            List<DestinationProduction> destinationsLivreur,
            List<DestinationProduction> destinationsBoutique,
            List<DestinationProduction> destinationsRations,
            List<DestinationProduction> destinationsAumone,
            List<DestinationProduction> destinationsPainGate
    ) {

        ProductionMensuelleReportDto report =
                new ProductionMensuelleReportDto()
                        .setPeriode(periode);

        /*
         * -----------------------------------------------
         * 1. PRODUCTION (vue par produit GP/PP)
         * -----------------------------------------------
         */
        construireProductions(
                report,
                lots
        );

        /*
         * -----------------------------------------------
         * 2. LIVREURS
         * -----------------------------------------------
         *
         * Les livreurs sont dynamiques.
         */
        construireLivreurs(
                report,
                destinationsLivreur
        );

        /*
         * -----------------------------------------------
         * 3. DESTINATIONS FIXES
         * -----------------------------------------------
         */
        report.setBoutique(
                construireDistribution(destinationsBoutique)
        );

        report.setRations(
                construireDistribution(destinationsRations)
        );

        report.setAumone(
                construireDistribution(destinationsAumone)
        );

        /*
         * Pain gâté / Pain frais : toutes les destinations
         * marquées EtatPain.GATE sur la période, tous canaux
         * confondus (cf. commentaire dans genererRapportMensuel).
         */
        report.setPainGatePainFrais(
                construireDistribution(destinationsPainGate)
        );

        /*
         * -----------------------------------------------
         * 4. CONSOMMABLES / RENDEMENT — dérivés des LOTS,
         *    pas seulement des lignes GP/PP filtrées, car les
         *    sacs de farine et la quantité prévue concernent
         *    TOUTE la production du jour (y compris les
         *    produits actuellement hors du rapport GP/PP,
         *    ex. Boulanger).
         * -----------------------------------------------
         */
        construireSacsEtRendementParJour(
                report,
                lots
        );

        construireConsommables(
                report,
                lots
        );

        /*
         * -----------------------------------------------
         * 5. TOTAUX
         * -----------------------------------------------
         */
        calculerTotauxGlobaux(report);

        return report;
    }

    /*
     * ===================================================
     * PRODUCTION
     * ===================================================
     */

    private void construireProductions(
            ProductionMensuelleReportDto report,
            List<LotProduction> lots
    ) {

        Map<Long, ProductionMensuelleLigneDto> lignes =
                new LinkedHashMap<>();

        Map<Long, Produit> produits =
                chargerProduits(lots);

        for (LotProduction lot : lots) {

            Produit produit =
                    produits.get(lot.getProduitId());

            if (produit == null) {
                throw new EntityNotFoundException(
                        "Produit introuvable : "
                                + lot.getProduitId()
                );
            }

            /*
             * Pour le moment, le rapport ne suit que :
             *
             * GP_1KG   -> GP
             * GP_1/2KG -> PP
             *
             * Pain mangé et Boulanger sont volontairement
             * ignorés.
             *
             * Pain frais / Pain gâté ne sont PAS des produits :
             * ils seront gérés par EtatPain au niveau des
             * destinations.
             */
            if (!estProduitProductionSuivi(produit)) {
                continue;
            }

            ProductionMensuelleLigneDto ligne =
                    lignes.computeIfAbsent(
                            produit.getId(),
                            id -> creerLigne(produit)
                    );

            ajouterLot(
                    ligne,
                    lot
            );
        }

        report.setProductions(
                new ArrayList<>(lignes.values())
        );
    }

    private Map<Long, Produit> chargerProduits(
            List<LotProduction> lots
    ) {

        Map<Long, Produit> produits =
                new HashMap<>();

        for (LotProduction lot : lots) {

            produits.computeIfAbsent(
                    lot.getProduitId(),
                    produitService::findProduitOrThrow
            );
        }

        return produits;
    }

    private boolean estProduitProductionSuivi(
            Produit produit
    ) {

        return switch (produit.getLibelle()) {

            case "GP_1KG",
                 "GP_1/2KG" -> true;

            /*
             * Ignorés pour le moment.
             */
            case "Pain mangé",
                 "Boulanger" -> false;

            /*
             * Tout autre produit n'entre pas dans
             * le rapport GP / PP actuel.
             */
            default -> false;
        };
    }

    private ProductionMensuelleLigneDto creerLigne(
            Produit produit
    ) {

        return new ProductionMensuelleLigneDto()
                .setProduitId(produit.getId())
                .setProduitLibelle(produit.getLibelle())
                .setTypeProduit(
                        determinerTypeProduction(produit)
                );
    }

    private String determinerTypeProduction(
            Produit produit
    ) {

        return switch (produit.getLibelle()) {

            case "GP_1KG" ->
                    "GP";

            case "GP_1/2KG" ->
                    "PP";

            default ->
                    "AUTRE";
        };
    }

    /*
     * Répartit chaque lot dans quantitesGp/quantitesPp ou
     * quantiteGpTotale/quantitePpTotale selon le type de la
     * ligne, au lieu d'un unique champ "quantites" générique
     * qui n'existe pas sur ProductionMensuelleLigneDto.
     */
    private void ajouterLot(
            ProductionMensuelleLigneDto ligne,
            LotProduction lot
    ) {

        BigDecimal realisee =
                safe(lot.getQuantiteRealisee());

        BigDecimal prevue =
                safe(lot.getQuantitePrevue());

        BigDecimal sacs =
                safe(lot.getSacsFarineUtilises());

        boolean estGp =
                "GP".equals(ligne.getTypeProduit());

        if (estGp) {

            ligne.getQuantitesGp().merge(
                    lot.getDate(),
                    realisee,
                    BigDecimal::add
            );

            ligne.setQuantiteGpTotale(
                    ligne.getQuantiteGpTotale().add(realisee)
            );

        } else {

            ligne.getQuantitesPp().merge(
                    lot.getDate(),
                    realisee,
                    BigDecimal::add
            );

            ligne.setQuantitePpTotale(
                    ligne.getQuantitePpTotale().add(realisee)
            );
        }

        ligne.setQuantitePrevue(
                ligne.getQuantitePrevue().add(prevue)
        );

        ligne.setSacsFarineUtilises(
                ligne.getSacsFarineUtilises().add(sacs)
        );
    }

    /*
     * ===================================================
     * SACS DE FARINE / RENDEMENT SOUHAITÉ — PAR JOUR
     * ===================================================
     *
     * Ces deux maps viennent directement des lots (pas de la
     * vue "productions" filtrée), car un sac de farine ou une
     * quantité prévue engagent TOUTE la production du jour,
     * indépendamment du produit.
     *
     * Important : quantitePrevue est déjà calculée à la création
     * du lot via Recette.calculerQuantitePrevue(multiplicateur),
     * qui dépend du rendement PROPRE à la recette utilisée. On ne
     * réintroduit donc PAS de constante (ex. 340) ici : on fait
     * simplement la somme de ce qui a déjà été calculé au bon
     * endroit (le domaine), pour chaque jour.
     */
    private void construireSacsEtRendementParJour(
            ProductionMensuelleReportDto report,
            List<LotProduction> lots
    ) {

        Map<LocalDate, BigDecimal> sacsParJour =
                new LinkedHashMap<>();

        Map<LocalDate, BigDecimal> prevueParJour =
                new LinkedHashMap<>();

        for (LotProduction lot : lots) {

            sacsParJour.merge(
                    lot.getDate(),
                    safe(lot.getSacsFarineUtilises()),
                    BigDecimal::add
            );

            prevueParJour.merge(
                    lot.getDate(),
                    safe(lot.getQuantitePrevue()),
                    BigDecimal::add
            );
        }

        report.setSacsFarineParJour(sacsParJour);
        report.setQuantitesPrevuesParJour(prevueParJour);
    }

    /*
     * ===================================================
     * LEVURE / AMÉLIORANT
     * ===================================================
     *
     * Ce sont des quantités FIXES par recette (déclarées telles
     * quelles sur RecetteIngredient.quantite), pas mises à
     * l'échelle par le nombre de sacs de farine du lot. On va
     * donc chercher directement les RecetteIngredient de la
     * recette du lot, sans passer par
     * calculerMultiplicateur/calculerConsommations (qui scalent
     * bien la quantité prévue de pain, mais pas la levure/
     * l'améliorant ici).
     *
     * Un lot compte une fois par jour, quel que soit
     * sacsFarineUtilises — cette valeur n'entre plus en jeu pour
     * ce calcul.
     */
    private void construireConsommables(
            ProductionMensuelleReportDto report,
            List<LotProduction> lots
    ) {

        Map<LocalDate, BigDecimal> levureParJour =
                new LinkedHashMap<>();

        Map<LocalDate, BigDecimal> ameliorantParJour =
                new LinkedHashMap<>();

        Map<Long, List<RecetteIngredient>> ingredientsParRecette =
                new HashMap<>();

        for (LotProduction lot : lots) {

            List<RecetteIngredient> ingredients =
                    ingredientsParRecette.computeIfAbsent(
                            lot.getRecetteId(),
                            this::trouverIngredientsDeRecette
                    );

            for (RecetteIngredient recetteIngredient : ingredients) {

                Ingredient ingredient = recetteIngredient.getIngredient();

                if (ingredient.estLevure()) {

                    levureParJour.merge(
                            lot.getDate(),
                            safe(recetteIngredient.getQuantite()),
                            BigDecimal::add
                    );

                } else if (ingredient.estAmeliorant()) {

                    ameliorantParJour.merge(
                            lot.getDate(),
                            safe(recetteIngredient.getQuantite()),
                            BigDecimal::add
                    );
                }
            }
        }

        report.setLevureParJour(levureParJour);
        report.setAmeliorantParJour(ameliorantParJour);
    }

    private List<RecetteIngredient> trouverIngredientsDeRecette(
            Long recetteId
    ) {

        return recetteIngredientRepository.findByRecetteId(recetteId);
    }

    /*
     * ===================================================
     * LIVREURS
     * ===================================================
     */

    private void construireLivreurs(
            ProductionMensuelleReportDto report,
            List<DestinationProduction> destinations
    ) {

        /*
         * On groupe les destinations par livreur.
         *
         * Aucun nombre fixe de livreurs.
         */
        Map<Long, List<DestinationProduction>> parLivreur =
                destinations.stream()
                        .filter(destination ->
                                destination.getLivreurId() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        DestinationProduction::getLivreurId,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        List<LivreurProductionMensuelleDto> livreurs =
                new ArrayList<>();

        for (Map.Entry<Long, List<DestinationProduction>> entry
                : parLivreur.entrySet()) {

            Long livreurId = entry.getKey();

            LivreurProductionMensuelleDto dto =
                    construireLivreur(
                            livreurId,
                            entry.getValue()
                    );

            livreurs.add(dto);
        }

        report.setLivreurs(livreurs);
    }

    /*
     * Corrigé pour construire LivreurProductionMensuelleDto
     * (le type réellement lu par l'exporteur Excel), avec les
     * bons noms de champs : nom / quantiteGpTotale /
     * quantitePpTotale — au lieu de livreurNom / totalGp /
     * totalPp / totalKilo qui n'existent pas sur ce DTO.
     */
    private LivreurProductionMensuelleDto construireLivreur(
            Long livreurId,
            List<DestinationProduction> destinations
    ) {

        Livreur livreur = livreurService.findLivreurById(livreurId);

        LivreurProductionMensuelleDto dto =
                new LivreurProductionMensuelleDto()
                        .setLivreurId(livreurId)
                        .setNom(
                                livreur.getPrenom()
                                        + " "
                                        + livreur.getNom()
                        );

        for (DestinationProduction destination
                : destinations) {

            Produit produit =
                    produitService.findProduitOrThrow(
                            destination.getProduitId()
                    );

            BigDecimal quantite =
                    safe(destination.getQuantite());

            LocalDate date =
                    destination.getDate();

            switch (produit.getLibelle()) {

                /*
                 * GP = 1 kg
                 */
                case "GP_1KG" -> {

                    dto.getQuantitesGp().merge(
                            date,
                            quantite,
                            BigDecimal::add
                    );

                    dto.setQuantiteGpTotale(
                            dto.getQuantiteGpTotale()
                                    .add(quantite)
                    );
                }

                /*
                 * PP = 0,5 kg
                 */
                case "GP_1/2KG" -> {

                    dto.getQuantitesPp().merge(
                            date,
                            quantite,
                            BigDecimal::add
                    );

                    dto.setQuantitePpTotale(
                            dto.getQuantitePpTotale()
                                    .add(quantite)
                    );
                }

                /*
                 * Les autres produits sont ignorés
                 * dans le tableau GP / PP des livreurs.
                 */
                default -> {
                    // Rien à faire.
                }
            }
        }

        return dto;
    }

    /*
     * ===================================================
     * DESTINATIONS FIXES (Boutique / Rations / Aumône)
     * ===================================================
     *
     * Même logique que pour un livreur, mais sans groupement
     * par livreurId : toutes les destinations du canal sont
     * cumulées dans un seul ProductionDistributionMensuelleDto.
     */
    private ProductionDistributionMensuelleDto construireDistribution(
            List<DestinationProduction> destinations
    ) {

        ProductionDistributionMensuelleDto dto =
                new ProductionDistributionMensuelleDto();

        for (DestinationProduction destination : destinations) {

            Produit produit =
                    produitService.findProduitOrThrow(
                            destination.getProduitId()
                    );

            BigDecimal quantite =
                    safe(destination.getQuantite());

            LocalDate date =
                    destination.getDate();

            switch (produit.getLibelle()) {

                case "GP_1KG" -> {

                    dto.getQuantitesGp().merge(
                            date,
                            quantite,
                            BigDecimal::add
                    );

                    dto.setQuantiteGpTotale(
                            dto.getQuantiteGpTotale().add(quantite)
                    );
                }

                case "GP_1/2KG" -> {

                    dto.getQuantitesPp().merge(
                            date,
                            quantite,
                            BigDecimal::add
                    );

                    dto.setQuantitePpTotale(
                            dto.getQuantitePpTotale().add(quantite)
                    );
                }

                default -> {
                    // Rien à faire.
                }
            }
        }

        return dto;
    }

    /*
     * ===================================================
     * TOTAUX GLOBAUX
     * ===================================================
     */

    private void calculerTotauxGlobaux(
            ProductionMensuelleReportDto report
    ) {

        for (ProductionMensuelleLigneDto ligne
                : report.getProductions()) {

            report.setQuantitePrevueTotale(
                    report.getQuantitePrevueTotale()
                            .add(
                                    ligne.getQuantitePrevue()
                            )
            );

            report.setQuantiteRealiseeTotale(
                    report.getQuantiteRealiseeTotale()
                            .add(
                                    ligne.getQuantiteGpTotale()
                                            .add(
                                                    ligne.getQuantitePpTotale()
                                            )
                            )
            );

            report.setSacsFarineTotal(
                    report.getSacsFarineTotal()
                            .add(
                                    ligne.getSacsFarineUtilises()
                            )
            );
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}