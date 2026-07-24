// abonnements/repository/PaiementAbonnementRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.PaiementAbonnement;
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
}