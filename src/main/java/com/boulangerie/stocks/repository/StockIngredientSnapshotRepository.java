// stocks/repository/StockIngredientSnapshotRepository.java
package com.boulangerie.stocks.repository;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.stocks.model.StockIngredientSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockIngredientSnapshotRepository extends JpaRepository<StockIngredientSnapshot, Long> {

    Page<StockIngredientSnapshot> findByPeriodeId(Long periodeId, Pageable pageable);

    List<StockIngredientSnapshot> findByPeriodeIdAndIngredient(Long periodeId, Ingredient ingredient);


    // Delete old snapshots (for retention policy)
//    @Modifying
//    @Query("DELETE FROM StockIngredientSnapshot s WHERE s.periode.dateFin < :cutoff")
//    int deleteByPeriodeDateFinBefore(@Param("cutoff") LocalDate cutoff);

    // Alternative: delete by period
    @Modifying
    @Query("DELETE FROM StockIngredientSnapshot s WHERE s.periodeId = :periodeId")
    void deleteByPeriodeId(@Param("periodeId") Long periodeId);

//    @Modifying
//    @Query("DELETE FROM StockIngredientSnapshot s WHERE s.periode.dateDebut < :cutoff")
//    int deleteByPeriodeDateDebutBefore(@Param("cutoff") LocalDateTime cutoff);
}