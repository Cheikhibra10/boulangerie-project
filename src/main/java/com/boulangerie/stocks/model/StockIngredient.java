// stocks/model/StockIngredient.java
package com.boulangerie.stocks.model;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.stocks.exception.StockInsuffisantException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_ingredients")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class StockIngredient extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", unique = true, nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantite = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valeurTotale = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal seuilAlerte = BigDecimal.ZERO;

    public static StockIngredient creer(Ingredient ingredient) {

        StockIngredient stock = new StockIngredient();

        stock.ingredient = ingredient;
        stock.quantite = BigDecimal.ZERO;
        stock.valeurTotale = BigDecimal.ZERO;
        stock.seuilAlerte = ingredient.getSeuilAlerteParDefaut();
        return stock;
    }

    public void modifierSeuilAlerte(BigDecimal nouveauSeuil) {
        if (nouveauSeuil == null || nouveauSeuil.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le seuil d'alerte doit être supérieur à zéro.");
        }
        this.seuilAlerte = nouveauSeuil;
    }

    public void ajouter(BigDecimal quantite, BigDecimal montant) {
        this.quantite = this.quantite.add(quantite);
        this.valeurTotale = this.valeurTotale.add(montant);
    }

    public void retirer(BigDecimal quantite, BigDecimal montant) {

        verifierDisponibilite(quantite);
        this.quantite = this.quantite.subtract(quantite);
        this.valeurTotale = this.valeurTotale.subtract(montant);
    }

    public void verifierDisponibilite(BigDecimal quantiteDemandee) {
        if (quantite.compareTo(quantiteDemandee) < 0) {
            throw new StockInsuffisantException(ingredient.getId());
        }
    }

    public boolean estSousSeuil() {

        return quantite.compareTo(seuilAlerte) < 0;
    }

    public void appliquerMouvement(
            TypeMouvementStock type,
            BigDecimal quantite,
            BigDecimal montant
    ) {

        switch (type) {
            case RECEPTION_ACHAT , AJUSTEMENT -> ajouter(quantite, montant);
            case CONSOMMATION_PRODUCTION, PERTE -> retirer(quantite, montant);
            default -> throw new IllegalArgumentException("Type de mouvement inconnu.");
        }
    }

    public void verifierMouvement(
            TypeMouvementStock type,
            BigDecimal quantite
    ) {

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité doit être positive.");
        }

        if (type == TypeMouvementStock.CONSOMMATION_PRODUCTION) {
            verifierDisponibilite(quantite);
        }
    }
}