// stocks/repository/MouvementStockRepository.java
package com.boulangerie.stocks.repository;

import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.TypeMouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface MouvementStockRepository
        extends JpaRepository<MouvementStock, Long>,
        JpaSpecificationExecutor<MouvementStock> {

    @Query("SELECT COALESCE(SUM(m.montant), 0) FROM MouvementStock m WHERE m.ingredient.id = :ingredientId AND m.type = :type")
    BigDecimal sumMontantByIngredientIdAndType(@Param("ingredientId") Long ingredientId, @Param("type") TypeMouvementStock type);

    List<MouvementStock> findByLotProductionId(Long lotProductionId);

    List<MouvementStock> findByLigneAchatId(Long ligneAchatId);
}