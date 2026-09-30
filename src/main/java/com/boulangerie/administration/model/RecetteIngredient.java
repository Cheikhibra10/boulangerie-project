package com.boulangerie.administration.model;

import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.shared.exception.BadRequestException;
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
        if (multiplicateur == null || multiplicateur.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le multiplicateur doit être positif.");
        }
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité de l'ingrédient doit être positive.");
        }
        return new ConsommationIngredient(ingredient.getId(),  quantite.multiply(multiplicateur)
        );
    }
}