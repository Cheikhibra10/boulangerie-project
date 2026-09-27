// production/repository/DestinationProductionRepository.java
package com.boulangerie.production.repository;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.model.EtatPain;
import com.boulangerie.production.model.LotProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationProductionRepository extends JpaRepository<DestinationProduction, Long> {
    List<DestinationProduction> findByLotId(Long lotId);

    List<DestinationProduction> findByDateAndCanal(LocalDate date, CanalDistribution canal);
    Optional<DestinationProduction> findFirstByProduitIdAndCanalOrderByDateDesc(
            Long produitId,
            CanalDistribution canal
    );
    List<DestinationProduction> findByDateAndLivreurId(LocalDate date, Long livreurId);
    boolean existsByDateAndLivreurId(LocalDate date, Long livreur);

    @Query("SELECT COALESCE(SUM(d.quantite), 0) FROM DestinationProduction d WHERE d.lot.id = :lotId")
    BigDecimal sumQuantiteByLotId(@Param("lotId") Long lotId);

    @Query("""
        select d.lot.id as lotId, coalesce(sum(d.quantite), 0) as total
        from DestinationProduction d
        where d.lot.id in :lotIds
        group by d.lot.id
        """)
    List<LotQuantiteDistribueeProjection> sumQuantiteByLotIds(@Param("lotIds") List<Long> lotIds);

    @Query("""
            select coalesce(sum(d.quantite),0)
            from DestinationProduction d
            where d.abonnementId = :abonnementId
            and d.date = :date
            """)
    BigDecimal sumQuantiteDistribuee(
            @Param("abonnementId") Long abonnementId,
            @Param("date") LocalDate date
    );

    @Query("SELECT d FROM DestinationProduction d JOIN FETCH d.lot WHERE d.id = :id")
    Optional<DestinationProduction> findByIdWithDetails(@Param("id") Long id);

    List<DestinationProduction> findByDateBetweenAndCanal(
            LocalDate debut,
            LocalDate fin,
            CanalDistribution canal
    );

    List<DestinationProduction> findByDateBetweenAndEtatPain(LocalDate debut, LocalDate fin, EtatPain etatPain);
}

