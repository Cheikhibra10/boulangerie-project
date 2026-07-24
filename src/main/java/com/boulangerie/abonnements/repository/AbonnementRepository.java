// abonnements/repository/AbonnementRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.Abonnement;
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
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {
    Page<Abonnement> findByActifTrue(Pageable pageable);
    @Query("SELECT COALESCE(SUM(c.quantite * l.prixUnitaire), 0) " +
            "FROM ConsommationJournaliere c " +
            "JOIN c.ligne l " +
            "WHERE c.date BETWEEN :debut AND :fin")
    BigDecimal sumCaBetweenDates(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(l.reliquat), 0) FROM LigneAbonnement l WHERE l.reliquat > 0")
    BigDecimal sumCreditsClients();

    @Query("SELECT COUNT(a) FROM Abonnement a WHERE a.actif = true")
    Integer countByActifTrue();

    @Query("SELECT a FROM Abonnement a JOIN FETCH a.lignes l JOIN FETCH l.client.id WHERE a.id = :id")
    Optional<Abonnement> findByIdWithLignesAndClients(@Param("id") Long id);
}