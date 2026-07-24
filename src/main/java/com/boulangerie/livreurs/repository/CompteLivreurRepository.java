// livreurs/repository/CompteLivreurRepository.java
package com.boulangerie.livreurs.repository;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.livreurs.model.CompteLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CompteLivreurRepository extends JpaRepository<CompteLivreur, Long> {

    Optional<CompteLivreur> findByLivreurId(Long livreurId);


    @Query("SELECT COUNT(DISTINCT c.livreurId) FROM CompteLivreur c WHERE c.soldeActuel > 0")
    Integer countLivreursActifs();
    @Query("SELECT COALESCE(SUM(l.montantApresDeduction), 0) " +
            "FROM LigneCompteLivreur l " +
            "JOIN l.journalier j " +
            "WHERE j.date BETWEEN :debut AND :fin")
    BigDecimal sumCaLivreursBetweenDates(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.soldeActuel), 0) FROM CompteLivreur c")
    BigDecimal sumSoldeActuel();
    }