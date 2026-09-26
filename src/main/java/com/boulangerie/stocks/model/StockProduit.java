package com.boulangerie.stocks.model;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.StockConstants;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.stocks.exception.StockInsuffisantException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "stock_produit")
public class StockProduit extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Verrou optimiste : sans lui, deux ventes concurrentes qui lisent la
    // même quantité de stock, la décrémentent chacune en mémoire, puis
    // sauvegardent, écrasent silencieusement le résultat l'une de l'autre
    // (perte de mise à jour) — c'est exactement le mécanisme d'une
    // survente. Avec @Version, la seconde sauvegarde échoue avec
    // OptimisticLockException (mappée en 409 par GlobalExceptionHandler)
    // plutôt que de corrompre silencieusement la quantité en stock.
    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", unique = true, nullable = false)
    private Produit produit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantite = BigDecimal.ZERO;

    @Column(name = "valeur_totale", nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurTotale = BigDecimal.ZERO;

    @Column(name = "seuil_alerte", nullable = false, precision = 15, scale = 2)
    private BigDecimal seuilAlerte = StockConstants.SEUIL_PRODUIT;

    public void ajouter(BigDecimal quantite) {
        verifierQuantitePositive(quantite);
        this.quantite = this.quantite.add(quantite);
        this.valeurTotale = this.quantite.multiply(this.produit.getPrixDetail());
    }

    public void retirer(BigDecimal quantite) {
        verifierDisponibilite(quantite);
        this.quantite = this.quantite.subtract(quantite);
        this.valeurTotale = this.quantite.multiply(this.produit.getPrixDetail());
    }

    public void verifierDisponibilite(BigDecimal quantite) {
        verifierQuantitePositive(quantite);

        if (this.quantite.compareTo(quantite) < 0) {
            throw new StockInsuffisantException(produit.getId());
        }
    }

    public boolean estSousSeuil() {
        return quantite.compareTo(seuilAlerte) < 0;
    }

    private void verifierQuantitePositive(BigDecimal quantite) {
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "La quantité doit être supérieure à zéro."
            );
        }
    }
}