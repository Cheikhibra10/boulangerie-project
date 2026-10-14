package com.boulangerie.rh.repository;

import com.boulangerie.rh.model.Absence;
import com.boulangerie.rh.model.StatutAbsence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long>,
        JpaSpecificationExecutor<Absence> {
    @Query("""
    SELECT a FROM Absence a
    WHERE a.employe.id = :employeId
      AND a.statut = :statut
      AND a.dateDebut <= :fin
      AND a.dateFin >= :debut
    """)
    List<Absence> findApprouveesOverlapping(
            @Param("employeId") Long employeId,
            @Param("statut") StatutAbsence statut,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("""
            SELECT a FROM Absence a
            JOIN FETCH a.employe e
            WHERE a.statut = :statut
              AND a.dateDebut <= :fin
              AND a.dateFin >= :debut
            """)
    List<Absence> findApprouveesOverlappingRange(
            @Param("statut") StatutAbsence statut,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("""
    SELECT
        a.employe.id          AS employeId,
        a.employe.prenom      AS prenom,
        a.employe.nom         AS nom,
        COUNT(a)              AS nombreAbsences,
        COALESCE(
            SUM(
                CAST(
                    (LEAST(a.dateFin, :dateFin) - GREATEST(a.dateDebut, :dateDebut)) AS integer
                ) + 1
            ), 0
        ) AS joursAbsents
    FROM Absence a
    WHERE a.statut = :statut
      AND a.dateDebut <= :dateFin
      AND a.dateFin   >= :dateDebut
    GROUP BY a.employe.id, a.employe.prenom, a.employe.nom
    """)
    List<AbsenceParEmployeProjection> aggregateApprouveesByEmploye(
            @Param("statut") StatutAbsence statut,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );
}