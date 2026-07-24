// stocks/repository/StockInitialRepository.java
package com.boulangerie.stocks.repository;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.stocks.model.StockInitial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockInitialRepository extends JpaRepository<StockInitial, Long> {

    Optional<StockInitial> findByPeriodeIdAndIngredient(Long periodeId, Ingredient ingredient);

    boolean existsByPeriodeIdAndIngredient(Long periodeId, Ingredient ingredient);
}