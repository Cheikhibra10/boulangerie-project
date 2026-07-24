// livreurs/repository/CompteLivreurJournalierRepository.java
package com.boulangerie.livreurs.repository;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.StatutCompteRendu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CompteLivreurJournalierRepository extends JpaRepository<CompteLivreurJournalier, Long> {

    Optional<CompteLivreurJournalier> findByLivreurIdAndDate(Long livreurId, LocalDate date);

    Page<CompteLivreurJournalier> findByLivreurIdOrderByDateDesc(Long livreurId, Pageable pageable);

    @Query("SELECT c FROM CompteLivreurJournalier c WHERE c.livreurId = :livreurId AND c.date BETWEEN :debut AND :fin ORDER BY c.date DESC")
    Page<CompteLivreurJournalier> findByLivreurIdAndDateBetween(
            @Param("livreurId") Long livreurId,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "lignes"
    })
    Optional<CompteLivreurJournalier> findById(Long id);

    Optional<CompteLivreurJournalier> findTopByLivreurIdOrderByDateDesc(Long livreurId);

    @Query("SELECT COALESCE(SUM(l.montantApresDeduction), 0) " +
            "FROM LigneCompteLivreur l " +
            "JOIN l.journalier j " +
            "WHERE j.date BETWEEN :debut AND :fin")
    BigDecimal sumCaLivreursBetweenDates(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.soldeActuel), 0) FROM CompteLivreur c")
    BigDecimal sumSoldeActuel();
}