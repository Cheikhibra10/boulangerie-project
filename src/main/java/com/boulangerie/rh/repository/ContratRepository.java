package com.boulangerie.rh.repository;

import com.boulangerie.rh.model.Contrat;
import com.boulangerie.rh.model.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ContratRepository
        extends JpaRepository<Contrat, Long>,
        JpaSpecificationExecutor<Contrat> {

    boolean existsByEmployeIdAndStatut(Long employeId, StatutContrat statut);

    Optional<Contrat> findFirstByEmployeIdAndStatut(Long employeId, StatutContrat statut);

    /**
     * Active contract that overlaps the accounting period.
     */
    @Query("""
            SELECT c FROM Contrat c
            WHERE c.employe.id = :employeId
              AND c.statut = :statut
              AND c.dateDebut <= :periodeFin
              AND (c.dateFin IS NULL OR c.dateFin >= :periodeDebut)
            ORDER BY c.dateDebut DESC
            """)
    Optional<Contrat> findActifCouvrantPeriode(
            @Param("employeId") Long employeId,
            @Param("statut") StatutContrat statut,
            @Param("periodeDebut") LocalDate periodeDebut,
            @Param("periodeFin") LocalDate periodeFin
    );
}