// production/repository/LotProductionRepository.java
package com.boulangerie.production.repository;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.production.model.LotProduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface LotProductionRepository extends JpaRepository<LotProduction, Long> {

    Optional<LotProduction> findByDateAndProduitId(LocalDate date, Long produitId);

    boolean existsByDateAndProduitId(LocalDate date, Long produitId);

    Page<LotProduction> findByDateBetween(LocalDate debut, LocalDate fin, Pageable pageable);

    Page<LotProduction> findByProduitIdOrderByDateDesc(Long produitId, Pageable pageable);

    @Query("SELECT lp FROM LotProduction lp JOIN FETCH lp.produitId WHERE lp.id = :id")
    Optional<LotProduction> findByIdWithProduit(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(l.quantiteRealisee), 0) FROM LotProduction l " +
            "WHERE l.date BETWEEN :debut AND :fin AND l.quantiteRealisee IS NOT NULL")
    BigDecimal sumQuantiteRealiseeBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("SELECT COALESCE(SUM(l.quantitePrevue), 0) FROM LotProduction l " +
            "WHERE l.date BETWEEN :debut AND :fin")
    BigDecimal sumQuantitePrevueBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );
}