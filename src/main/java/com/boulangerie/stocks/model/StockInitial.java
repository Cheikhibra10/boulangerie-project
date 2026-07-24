// stocks/model/StockInitial.java
package com.boulangerie.stocks.model;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_initial", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"periode_id", "ingredient_id"})
})
@Getter
@Setter
@Accessors(chain = true)
public class StockInitial extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periode_id", nullable = false)
    private Long periodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantite", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantite;

    @Column(name = "valeur", precision = 15, scale = 2, nullable = false)
    private BigDecimal valeur;

    @Column(name = "cout_moyen_pondere", precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal coutMoyenPondere;
}