package com.boulangerie.administration.model;

import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.administration.service.ProductionConstants;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recettes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"produit_id", "version"}))
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Recette extends AbstractAuditingEntity
        implements GenericEntity<Recette> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private Boolean actif = true;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rendement;

    @Column(name = "temps_preparation")
    private Integer tempsPreparation;

    @Column(name = "temps_cuisson")
    private Integer tempsCuisson;

    @OneToMany(
            mappedBy = "recette",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private final List<RecetteIngredient> ingredients = new ArrayList<>();

    @Override
    public Recette createNewInstance() {
        return new Recette();
    }

    /**
     * Ajoute un ingrédient à la recette.
     */
    public void addIngredient(RecetteIngredient ingredient) {

        ingredient.setRecette(this);

        this.ingredients.add(ingredient);
    }

    /**
     * Retourne l'ingrédient représentant la farine.
     */
    public RecetteIngredient getFarine() {

        return ingredients.stream()
                .filter(i -> i.getIngredient().estFarine())
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "La recette doit contenir un ingrédient farine."
                        ));
    }

    /**
     * Vérifie que la recette possède une farine.
     */
    public boolean contientFarine() {

        return ingredients.stream()
                .anyMatch(i -> i.getIngredient().estFarine());
    }

    /**
     * Calcule le multiplicateur de production.
     *
     * Exemple :
     *
     * Recette = 50 kg farine
     *
     * Production = 5 sacs
     *
     * 5 sacs = 250 kg
     *
     * multiplicateur = 250 / 50 = 5
     */
    public BigDecimal calculerMultiplicateur(BigDecimal sacsFarineUtilises) {

        BigDecimal farineDisponible = sacsFarineUtilises.multiply(ProductionConstants.POIDS_SAC_FARINE);
        return farineDisponible.divide(
                getFarine().getQuantite(),
                6,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Calcule la quantité théorique produite.
     */
    public BigDecimal calculerQuantitePrevue(BigDecimal multiplicateur) {
        return rendement.multiply(multiplicateur);
    }

    /**
     * Calcule toutes les consommations
     * d'ingrédients.
     */
    public List<ConsommationIngredient> calculerConsommations(BigDecimal multiplicateur) {
        return ingredients.stream()
                .map(i -> i.calculerConsommation(multiplicateur))
                .toList();
    }

    public void verifierPeutProduire() {

        if (Boolean.FALSE.equals(actif)) {
            throw new BadRequestException(
                    "La recette est inactive."
            );
        }

        if (ingredients.isEmpty()) {
            throw new BadRequestException(
                    "La recette ne contient aucun ingrédient."
            );
        }
        getFarine();
    }


}