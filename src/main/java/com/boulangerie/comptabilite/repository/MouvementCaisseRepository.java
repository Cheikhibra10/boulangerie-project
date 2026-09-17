// caisse/repository/MouvementCaisseRepository.java
package com.boulangerie.comptabilite.repository;

import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.shared.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MouvementCaisseRepository
        extends JpaRepository<MouvementCaisse, Long>, JpaSpecificationExecutor<MouvementCaisse> {

    // Les méthodes spécifiques peuvent être remplacées par Specification
    // On garde quelques utilitaires pour les agrégations

    List<MouvementCaisse> findByCreatedAtBetweenOrderByCreatedAtAsc(
            Instant debut,
            Instant fin
    );

    @Query("SELECT COALESCE(SUM(m.montant), 0) FROM MouvementCaisse m WHERE m.caisse.id = :caisseId AND m.sens = :sens")
    BigDecimal sumMontantByCaisseIdAndSens(@Param("caisseId") Long caisseId, @Param("sens") SensMouvement sens);

    @Query("SELECT COALESCE(SUM(m.montant), 0) FROM MouvementCaisse m " +
            "WHERE m.sens = 'SORTIE' AND DATE(m.createdAt) BETWEEN :debut AND :fin")
    BigDecimal sumChargesBetweenDates(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(m.montant), 0) FROM MouvementCaisse m " +
            "WHERE m.sens = 'ENTREE' AND DATE(m.createdAt) BETWEEN :debut AND :fin")
    BigDecimal sumEntreesBetweenDates(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(m.montant), 0) FROM MouvementCaisse m " +
            "WHERE m.typeMouvement = :type AND m.sens = :sens AND DATE(m.createdAt) BETWEEN :debut AND :fin")
    BigDecimal sumByTypeAndSensBetweenDates(
            @Param("type") TypeMouvement type,
            @Param("sens") SensMouvement sens,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );
}