// abonnements/repository/ConsommationJournaliereRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.ConsommationJournaliere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}