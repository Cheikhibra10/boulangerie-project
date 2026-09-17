// abonnements/repository/PaiementAbonnementRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.PaiementAbonnement;
import com.boulangerie.abonnements.projection.PaiementReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaiementAbonnementRepository extends JpaRepository<PaiementAbonnement, Long> {

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PaiementAbonnement p WHERE p.ligne.id = :ligneId")
    BigDecimal sumMontantByLigneId(@Param("ligneId") Long ligneId);

    @Query("""
            select p
            from PaiementAbonnement p
            where p.ligne.abonnement.id = :abonnementId
              and p.createdAt between :debut and :fin
            """)
    List<PaiementAbonnement> findMensuels(
            @Param("abonnementId") Long abonnementId,
            @Param("debut") Instant debut,
            @Param("fin") Instant fin
    );

    @Query("""
    select p
    from PaiementAbonnement p
    join fetch p.ligne l
    where l.abonnement.id = :abonnementId
      and p.createdAt >= :debut
      and p.createdAt < :fin
    order by p.createdAt
    """)
    List<PaiementAbonnement> findByAbonnementIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            @Param("abonnementId") Long abonnementId,
            @Param("debut") Instant debut,
            @Param("fin") Instant fin
    );

    @Query("""
    SELECT p
    FROM PaiementAbonnement p
    JOIN FETCH p.ligne l
    WHERE l.abonnement.id IN :abonnementIds
      AND p.createdAt >= :debut
      AND p.createdAt < :fin
    ORDER BY p.createdAt
    """)
    List<PaiementAbonnement>
    findByAbonnementIdInAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            List<Long> abonnementIds,
            Instant debut,
            Instant fin
    );

    @Query("""
    SELECT
        p.id AS paiementId,
        l.id AS ligneId,
        a.id AS abonnementId,
        p.montant AS montant,
        p.createdAt AS createdAt
    FROM PaiementAbonnement p
    JOIN p.ligne l
    JOIN l.abonnement a
    WHERE a.id = :abonnementId
      AND p.createdAt >= :debut
      AND p.createdAt < :fin
    ORDER BY l.id, p.createdAt
""")
    List<PaiementReportProjection> findReportData(
            @Param("abonnementId") Long abonnementId,
            @Param("debut") Instant debut,
            @Param("fin") Instant fin
    );

    @Query("""
    SELECT
        p.id AS paiementId,
        l.id AS ligneId,
        a.id AS abonnementId,
        p.montant AS montant,
        p.createdAt AS createdAt
    FROM PaiementAbonnement p
    JOIN p.ligne l
    JOIN l.abonnement a
    WHERE a.id IN :abonnementIds
      AND p.createdAt >= :debut
      AND p.createdAt < :fin
    ORDER BY l.id, p.createdAt
""")
    List<PaiementReportProjection> findReportData(
            @Param("abonnementIds") List<Long> abonnementId,
            @Param("debut") Instant debut,
            @Param("fin") Instant fin
    );

}