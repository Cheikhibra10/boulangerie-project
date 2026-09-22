package com.boulangerie.achats.model;

import com.boulangerie.achats.exception.ValeurInvalideException;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "lignes_achat")
@Getter
@Setter(AccessLevel.PACKAGE) // Only ORM and aggregate can modify
public class LigneAchat extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achat_id", nullable = false)
    private Achat achat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantite", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantiteCommandee;

    @Column(name = "quantite_recue", precision = 15, scale = 2)
    private BigDecimal quantiteRecue = BigDecimal.ZERO;

    @Column(name = "quantite_retournee", precision = 15, scale = 2)
    private BigDecimal quantiteRetournee = BigDecimal.ZERO;

    @Column(name = "motif_retour")
    private String motifRetour;

    @Column(name = "prix_unitaire", precision = 15, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;


    // ===== FACTORY METHOD =====
    static LigneAchat creer(Achat achat, Ingredient ingredient, BigDecimal quantite, BigDecimal prixUnitaire) {
        LigneAchat ligne = new LigneAchat();
        ligne.achat = achat;
        ligne.ingredient = ingredient;
        ligne.quantiteCommandee = quantite;
        ligne.quantiteRecue = BigDecimal.ZERO;
        ligne.quantiteRetournee = BigDecimal.ZERO;
        ligne.prixUnitaire = prixUnitaire;
        return ligne;
    }

    // ===== BUSINESS METHODS =====
    public void modifier(BigDecimal nouvelleQuantite, BigDecimal nouveauPrix) {
        validatePositive(nouvelleQuantite, "Quantité");
        validatePositive(nouveauPrix, "Prix unitaire");
        if (nouvelleQuantite.compareTo(quantiteRecue) < 0) {
            throw new BadRequestException(
                    "La quantité commandée ne peut pas être inférieure à la quantité déjà reçue."
            );
        }
        if (quantiteRecue.compareTo(BigDecimal.ZERO) > 0 && nouveauPrix.compareTo(prixUnitaire) != 0) {
            throw new BadRequestException(
                    "Le prix unitaire ne peut plus être modifié après une réception."
            );
        }
        this.quantiteCommandee = nouvelleQuantite;
        this.prixUnitaire = nouveauPrix;
    }

    // ===== QUERY METHODS =====
    public BigDecimal getMontantCommande() {
        return this.quantiteCommandee.multiply(this.prixUnitaire);
    }

    // ===== PRIVATE HELPERS =====
    private void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValeurInvalideException(fieldName + " invalide");
        }
    }


    public void recevoir(BigDecimal quantiteRecue, BigDecimal quantiteRetournee, String motif) {

        BigDecimal quantiteRefusee = quantiteRetournee == null ? BigDecimal.ZERO : quantiteRetournee;
        validatePositive(quantiteRecue, "Quantité reçue");
        validatePositive(quantiteRefusee, "Quantité refusée");

        BigDecimal total = quantiteRecue.add(quantiteRefusee);
        BigDecimal nouvelleQuantite = this.quantiteRecue.add(quantiteRecue);
        BigDecimal restante = getQuantiteRestante();

        if (nouvelleQuantite.compareTo(quantiteCommandee) > 0) {
            throw new BadRequestException("La quantité reçue dépasse la quantité commandée");
        }

        if (total.compareTo(quantiteCommandee) > 0) {
            throw new BadRequestException("La quantité reçue dépasse la quantité commandée.");
        }

        if (total.compareTo(restante) > 0) {
            throw new BadRequestException("La réception dépasse la quantité restante.");
        }


        if (total.signum() == 0) {
            throw new BadRequestException("Aucune quantité renseignée.");
        }

        if (quantiteRefusee.signum() > 0 &&
                (motif == null || motif.isBlank())) {
            throw new BadRequestException("Le motif de refus est obligatoire.");
        }

        this.quantiteRecue = nouvelleQuantite;
        this.quantiteRetournee = this.quantiteRetournee.add(quantiteRefusee);
        this.motifRetour = motif;
    }

    public void retourner(BigDecimal quantiteRetournee, String motif) {

        validatePositive(quantiteRetournee, "Quantité retournée");

        if (motif == null || motif.isBlank()) {
            throw new BadRequestException("Le motif du retour est obligatoire.");
        }

        if (this.quantiteRetournee.compareTo(getQuantiteDisponiblePourRetour()) > 0) {
            this.motifRetour = null;
        }

        if (quantiteRetournee.compareTo(getQuantiteAcceptee()) > 0) {
            throw new BadRequestException(
                    "Impossible de retourner plus que la quantité acceptée."
            );
        }

        this.quantiteRetournee = this.quantiteRetournee.add(quantiteRetournee);

        this.motifRetour = motif;
    }

    public BigDecimal getQuantiteDisponiblePourRetour() {
        return getQuantiteAcceptee();
    }

    public BigDecimal getTotalRecu() {
        return quantiteRecue.multiply(prixUnitaire);
    }

    public BigDecimal getQuantiteRestante() {
        return quantiteCommandee.subtract(quantiteRecue);
    }

    public BigDecimal getTotalCommande() {
        return quantiteCommandee.multiply(prixUnitaire);
    }

    public BigDecimal getQuantiteAcceptee() {
        return quantiteRecue.subtract(quantiteRetournee);
    }

    public BigDecimal getEcart() {
        return quantiteCommandee.subtract(quantiteRecue);
    }

    public boolean estTotalementRecue() {
        return quantiteRecue.compareTo(quantiteCommandee) == 0;
    }

    public boolean estRetournee() {
        return quantiteRetournee.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean estReceptionComplete() {
        return getQuantiteRestante().compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean estPartiellementRecue() {
        return quantiteRecue.compareTo(BigDecimal.ZERO) > 0
                && !estTotalementRecue();
    }

    public BigDecimal getMontantAccepte() {
        return getQuantiteAcceptee().multiply(prixUnitaire);
    }

    public BigDecimal getMontantRetourne() {
        return quantiteRetournee.multiply(prixUnitaire);
    }
}