// abonnements/repository/ConsommationJournaliereRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.ConsommationJournaliere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsommationJournaliereRepository extends JpaRepository<ConsommationJournaliere, Long> {

    Optional<ConsommationJournaliere> findByLigneIdAndDate(Long ligneId, LocalDate date);

    boolean existsByLigneIdAndDate(Long ligneId, LocalDate date);
    @Query("""
            select c
            from ConsommationJournaliere c
            where c.ligne.abonnement.id = :abonnementId
              and c.date between :debut and :fin
            """)
    List<ConsommationJournaliere> findMensuelles(
            @Param("abonnementId") Long abonnementId,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("""
    SELECT c
    FROM ConsommationJournaliere c
    JOIN FETCH c.ligne l
    WHERE l.abonnement.id IN :abonnementIds
      AND c.date BETWEEN :debut AND :fin
    ORDER BY c.date
""")
    List<ConsommationJournaliere> findByAbonnementIdInAndDateBetween(
            @Param("abonnementIds") List<Long> abonnementIds,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );


    @Query("SELECT SUM(c.quantite) FROM ConsommationJournaliere c WHERE c.ligne.abonnement.id = :abonnementId AND c.date = :date")
    BigDecimal sumQuantiteByAbonnementIdAndDate(Long abonnementId, LocalDate date);
}