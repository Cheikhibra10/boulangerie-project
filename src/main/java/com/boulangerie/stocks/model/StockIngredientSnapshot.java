// stocks/model/StockIngredientSnapshot.java
package com.boulangerie.stocks.model;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_ingredient_snapshots")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockIngredientSnapshot extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "quantite_solde", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantiteSolde;

    @Column(name = "valeur_solde", precision = 15, scale = 2, nullable = false)
    private BigDecimal valeurSolde;
}