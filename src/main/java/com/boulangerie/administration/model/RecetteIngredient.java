package com.boulangerie.administration.model;

import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "recette_ingredients",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recette_id", "ingredient_id"}))
@Getter
@Setter
@Accessors(chain = true)
public class RecetteIngredient extends AbstractAuditingEntity implements GenericEntity<RecetteIngredient> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recette_id", nullable = false)
    private Recette recette;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantite", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantite;

    @Override
    public RecetteIngredient createNewInstance() {
       return new RecetteIngredient();
    }

    public ConsommationIngredient calculerConsommation(BigDecimal multiplicateur) {

        return new ConsommationIngredient(ingredient.getId(),  quantite.multiply(multiplicateur)
        );
    }
}