package com.boulangerie.comptabilite.service.impl;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.comptabilite.dto.LigneMontantDto;
import com.boulangerie.comptabilite.dto.VersementsJourDto;
import com.boulangerie.comptabilite.dto.VersementsRapportMensuelDto;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.comptabilite.repository.MouvementCaisseRepository;
import com.boulangerie.comptabilite.service.VersementsReportingService;
import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.VersementLivreur;
import com.boulangerie.livreurs.repository.CompteLivreurJournalierRepository;
import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.StatutVente;
import com.boulangerie.ventes.repository.LigneVenteBoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VersementsReportingServiceImpl implements VersementsReportingService {

    private final LigneVenteBoutiqueRepository ligneVenteBoutiqueRepository;
    private final ProduitService produitService;
    private final CompteLivreurJournalierRepository compteLivreurJournalierRepository;
    private final LivreurService livreurService;
    private final MouvementCaisseRepository mouvementCaisseRepository;

    /*
     * Préfixe posé par VersementAbonnementFactory sur le libellé du
     * versement ("Versement abonnement - <Nom>"). On l'enlève pour
     * ré-étiqueter la ligne "credit <Nom>", conformément à la
     * terminologie métier utilisée dans le rapport.
     */
    private static final String PREFIXE_LIBELLE_ABONNEMENT =
            "Versement abonnement - ";

    @Override
    public VersementsRapportMensuelDto genererRapportMensuel(
            YearMonth periode
    ) {

        Objects.requireNonNull(
                periode,
                "La période est obligatoire."
        );

        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        Map<LocalDate, List<LigneMontantDto>> versementsParJour =
                new TreeMap<>();

        Map<LocalDate, List<LigneMontantDto>> fraisParJour =
                new TreeMap<>();

        ajouterVentesBoutique(versementsParJour, debut, fin);
        ajouterLivreurs(versementsParJour, debut, fin);
        ajouterMouvementsCaisse(versementsParJour, fraisParJour, debut, fin);

        return construireRapport(periode, versementsParJour, fraisParJour);
    }

    /*
     * ===================================================
     * BOUTIQUE / AUTRES PRODUITS
     * ===================================================
     *
     * Calculées à partir des lignes de vente (pas de
     * MouvementCaisse.PAIEMENT, qui est au niveau de la vente
     * entière et ne permet donc pas de distinguer pain / autres
     * produits). Net des retours, comme la convention déjà en
     * place dans LigneVenteBoutiqueRepository
     * (sumCaBoutiqueBetweenDates).
     */
    private void ajouterVentesBoutique(
            Map<LocalDate, List<LigneMontantDto>> versementsParJour,
            LocalDate debut,
            LocalDate fin
    ) {

        List<LigneVenteBoutique> lignes =
                ligneVenteBoutiqueRepository
                        .findByVenteDateBetweenAndVenteStatut(
                                debut,
                                fin,
                                StatutVente.PAYEE
                        );

        Set<Long> painIds = produitService.findIdsByType(TypeProduit.PAIN);

        Map<LocalDate, BigDecimal> boutiqueParJour = new TreeMap<>();
        Map<LocalDate, BigDecimal> autresProduitsParJour = new TreeMap<>();

        for (LigneVenteBoutique ligne : lignes) {

            LocalDate date = ligne.getVente().getDate();

            /*
             * getMontantDisponibleRetour() = (quantite -
             * quantiteRetournee) * prixUnitaire. The name describes
             * its other use (how much is still returnable), but the
             * formula is exactly the net-of-returns revenue we need
             * here too — reused rather than duplicated.
             */
            BigDecimal montantNet = ligne.getMontantDisponibleRetour();

            if (painIds.contains(ligne.getProduitId())) {
                boutiqueParJour.merge(date, montantNet, BigDecimal::add);
            } else {
                autresProduitsParJour.merge(
                        date,
                        montantNet,
                        BigDecimal::add
                );
            }
        }

        boutiqueParJour.forEach((date, montant) ->
                ajouterSiPositif(
                        versementsParJour,
                        date,
                        "Boutique",
                        montant
                )
        );

        autresProduitsParJour.forEach((date, montant) ->
                ajouterSiPositif(
                        versementsParJour,
                        date,
                        "Autres produits",
                        montant
                )
        );
    }

    /*
     * ===================================================
     * LIVREURS
     * ===================================================
     *
     * Chaque versement quotidien clôturé est scindé en deux lignes
     * de rapport quand le livreur avait un reliquat en entrant
     * dans la journée :
     *
     *  - "Paiement reliquat <Nom>" = min(reliquatReport, montant)
     *  - "<Nom>"                   = le reste (peut être nul si le
     *                                 versement ne fait que combler
     *                                 le reliquat)
     *
     * reliquatReport est déjà, par construction
     * (CompteLivreurJournalierFactory), égal au reliquatFin de la
     * veille — pas besoin d'aller chercher CompteLivreur.soldeActuel
     * séparément pour ce calcul jour par jour.
     */
    private void ajouterLivreurs(
            Map<LocalDate, List<LigneMontantDto>> versementsParJour,
            LocalDate debut,
            LocalDate fin
    ) {

        List<CompteLivreurJournalier> journaliers =
                compteLivreurJournalierRepository
                        .findByDateBetweenOrderByDateAsc(debut, fin);

        Map<Long, Livreur> livreursParId = new HashMap<>();

        for (CompteLivreurJournalier journalier : journaliers) {

            VersementLivreur versement = journalier.getVersement();

            if (versement == null) {
                // Compte rendu pas encore clôturé ce jour-là.
                continue;
            }

            Livreur livreur =
                    livreursParId.computeIfAbsent(
                            journalier.getLivreurId(),
                            livreurService::getEntityById
                    );

            String nom = nomComplet(livreur);

            BigDecimal reliquatReport = safe(journalier.getReliquatReport());
            BigDecimal montantVerse = safe(versement.getMontant());

            BigDecimal portionReliquat = reliquatReport.min(montantVerse);
            BigDecimal portionReguliere = montantVerse.subtract(portionReliquat);

            ajouterSiPositif(
                    versementsParJour,
                    journalier.getDate(),
                    "Paiement reliquat " + nom,
                    portionReliquat
            );

            ajouterSiPositif(
                    versementsParJour,
                    journalier.getDate(),
                    nom,
                    portionReguliere
            );
        }
    }

    /*
     * ===================================================
     * MOUVEMENTS DE CAISSE : credits abonnement + FRAIS
     * ===================================================
     *
     * MouvementCaisse est la source unique pour :
     *  - FRAIS (sens = SORTIE, essentiellement DEPENSE_PERIODE)
     *  - "credit <Nom>" (VERSEMENT_ABONNEMENT, sens = ENTREE)
     *
     * Les autres types ENTREE (PAIEMENT, VERSEMENT_LIVREUR...) sont
     * volontairement ignorés ici : Boutique et Livreurs viennent
     * déjà de leurs sources dédiées ci-dessus, à un niveau de détail
     * (par produit / par livreur) que MouvementCaisse ne porte pas.
     */
    private void ajouterMouvementsCaisse(
            Map<LocalDate, List<LigneMontantDto>> versementsParJour,
            Map<LocalDate, List<LigneMontantDto>> fraisParJour,
            LocalDate debut,
            LocalDate fin
    ) {

        Instant debutInstant = debut.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Instant finInstant = fin.atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();

        List<MouvementCaisse> mouvements =
                mouvementCaisseRepository
                        .findByCreatedAtBetweenOrderByCreatedAtAsc(
                                debutInstant,
                                finInstant
                        );

        for (MouvementCaisse mouvement : mouvements) {

            LocalDate date = mouvement.getCreatedAt()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

            if (mouvement.getSens() == SensMouvement.SORTIE && mouvement.getTypeMouvement() == TypeMouvement.DEPENSE_PERIODE) {

                ajouterSiPositif(
                        fraisParJour,
                        date,
                        nullSafe(mouvement.getLibelle()),
                        safe(mouvement.getMontant())
                );

                continue;
            }

            if (mouvement.getTypeMouvement() == TypeMouvement.VERSEMENT_ABONNEMENT) {

                ajouterSiPositif(
                        versementsParJour,
                        date,
                        "credit "
                                + extraireNomAbonnement(
                                mouvement.getLibelle()
                        ),
                        safe(mouvement.getMontant())
                );
            }
        }
    }

    private String extraireNomAbonnement(String libelle) {

        if (libelle != null
                && libelle.startsWith(PREFIXE_LIBELLE_ABONNEMENT)) {

            return libelle.substring(
                    PREFIXE_LIBELLE_ABONNEMENT.length()
            );
        }

        return nullSafe(libelle);
    }

    /*
     * ===================================================
     * ASSEMBLAGE FINAL
     * ===================================================
     */

    private VersementsRapportMensuelDto construireRapport(
            YearMonth periode,
            Map<LocalDate, List<LigneMontantDto>> versementsParJour,
            Map<LocalDate, List<LigneMontantDto>> fraisParJour
    ) {

        VersementsRapportMensuelDto rapport =
                new VersementsRapportMensuelDto()
                        .setPeriode(periode);

        SortedSet<LocalDate> toutesLesDates = new TreeSet<>();
        toutesLesDates.addAll(versementsParJour.keySet());
        toutesLesDates.addAll(fraisParJour.keySet());

        for (LocalDate date : toutesLesDates) {

            VersementsJourDto jour =
                       new VersementsJourDto()
                            .setDate(date)
                            .setVersements(
                                    versementsParJour.getOrDefault(
                                            date,
                                            List.of()
                                    )
                            )
                            .setFrais(
                                    fraisParJour.getOrDefault(
                                            date,
                                            List.of()
                                    )
                            );

            rapport.getJours().add(jour);
        }

        return rapport;
    }

    /*
     * ===================================================
     * HELPERS
     * ===================================================
     */

    private void ajouterSiPositif(
            Map<LocalDate, List<LigneMontantDto>> map,
            LocalDate date,
            String libelle,
            BigDecimal montant
    ) {

        if (montant == null || montant.signum() <= 0) {
            return;
        }

        map.computeIfAbsent(date, d -> new ArrayList<>())
                .add(new LigneMontantDto(libelle, montant));
    }

    private String nomComplet(Livreur livreur) {

        String prenom = livreur.getPrenom();
        String nom = livreur.getNom();

        if (prenom == null || prenom.isBlank()) {
            return nom;
        }

        return prenom + " " + nom;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}