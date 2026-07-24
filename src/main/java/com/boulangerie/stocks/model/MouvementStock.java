// stocks/model/MouvementStock.java
package com.boulangerie.stocks.model;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "mouvements_stock")
@Getter
@Setter
@Accessors(chain = true)
public class MouvementStock extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeMouvementStock type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantite", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantite;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "motif")
    private String motif;

    @Column(name = "lot_production_id")
    private Long lotProductionId;

    @Column(name = "ligne_achat_id")
    private Long ligneAchatId;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private StatutMouvement statut = StatutMouvement.VALIDE;

    public static MouvementStock creerReceptionAchat(Ingredient ingredient, BigDecimal quantite, BigDecimal montant, Long ligneAchatId, String reference) {
        return new MouvementStock()
                .setType(TypeMouvementStock.RECEPTION_ACHAT)
                .setIngredient(ingredient)
                .setQuantite(quantite)
                .setMontant(montant)
                .setDate(Instant.now())
                .setMotif(reference)
                .setLigneAchatId(ligneAchatId);
    }

    public static MouvementStock creerRetourFournisseur(Ingredient ingredient, BigDecimal quantite, BigDecimal montant, Long ligneAchatId, String motif) {

        return new MouvementStock()
                .setType(TypeMouvementStock.RETOUR_FOURNISSEUR)
                .setIngredient(ingredient)
                .setQuantite(quantite)
                .setMontant(montant)
                .setDate(Instant.now())
                .setMotif(motif)
                .setLigneAchatId(ligneAchatId);
    }
}