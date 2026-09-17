package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.model.RecetteIngredient;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecetteIngredientRepository extends GenericRepository<RecetteIngredient> {

    /**
     * Liste tous les ingrédients d'une recette avec leur détails
     * Utilisé pour : affichage complet d'une recette
     */
    @Query("SELECT ri FROM RecetteIngredient ri " +
           "JOIN FETCH ri.ingredient i " +
           "WHERE ri.recette.id = :recetteId")
    List<RecetteIngredient> findByRecetteIdWithIngredient(@Param("recetteId") Long recetteId);

    /**
     * Trouve un ingrédient spécifique dans une recette
     * Utilisé pour : vérification/mise à jour
     */
    Optional<RecetteIngredient> findByRecetteAndIngredient(Recette recette, Ingredient ingredient);

    /**
     * Supprime tous les ingrédients d'une recette
     * Utilisé pour : mise à jour d'une recette (suppression des anciens)
     */
    void deleteByRecette(Recette recette);

    /**
     * Vérifie si un ingrédient est utilisé dans une recette active
     * Utilisé pour : empêcher la suppression d'un ingrédient utilisé
     */
    @Query("SELECT CASE WHEN COUNT(ri) > 0 THEN true ELSE false END " +
           "FROM RecetteIngredient ri " +
           "JOIN ri.recette r " +
           "WHERE ri.ingredient.id = :ingredientId " +
           "AND r.actif = true")
    boolean existsByIngredientIdInActiveRecette(@Param("ingredientId") Long ingredientId);

    List<RecetteIngredient> findByRecetteId(Long recetteId);
}