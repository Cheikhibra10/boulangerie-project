// stocks/repository/StockIngredientRepository.java
package com.boulangerie.stocks.repository;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.stocks.model.StockIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockIngredientRepository extends JpaRepository<StockIngredient, Long> {

    Optional<StockIngredient> findByIngredientId(Long ingredientId);
    List<StockIngredient> findByIngredientIn(List<Ingredient> ingredients);
    Optional<StockIngredient> findByIngredient(Ingredient ingredient);
    @Query("SELECT s FROM StockIngredient s WHERE s.ingredient.id IN :ingredientIds")
    List<StockIngredient> findByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds);
    @Query("SELECT s FROM StockIngredient s WHERE s.quantite < s.seuilAlerte")
    List<StockIngredient> findByQuantiteLessThanSeuilAlerte();
    @Query("SELECT s FROM StockIngredient s WHERE s.quantite > 0")
    List<StockIngredient> findByQuantiteGreaterThan(BigDecimal quantite);
    @Query("SELECT COUNT(s) FROM StockIngredient s WHERE s.quantite < s.seuilAlerte")
    Integer countByQuantiteLessThanSeuilAlerte();
}