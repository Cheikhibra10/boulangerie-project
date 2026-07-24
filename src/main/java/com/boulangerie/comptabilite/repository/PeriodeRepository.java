// comptabilite/repository/PeriodeRepository.java
package com.boulangerie.comptabilite.repository;

import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.administration.model.StatutPeriode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// comptabilite/domain/repository/PeriodeRepository.java
@Repository
public interface PeriodeRepository extends JpaRepository<Periode, Long> {
    long countByStatut(StatutPeriode statut);

    Optional<Periode> findByStatut(StatutPeriode statut);

    Optional<Periode> findTopByStatutOrderByDateFinDesc(StatutPeriode statut);

    @Query("SELECT p FROM Periode p WHERE p.dateDebut <= :date AND p.dateFin >= :date AND p.statut = 'OUVERTE'")
    Optional<Periode> findOpenPeriodContainingDate(@Param("date") LocalDate date);

    @Query("""
    SELECT p
    FROM Periode p
    WHERE :date BETWEEN p.dateDebut AND p.dateFin
""")
    Optional<Periode> findByDate(LocalDate date);

    @Query("SELECT p FROM Periode p ORDER BY p.dateDebut DESC")
    Page<Periode> findAllByOrderByDateDebutDesc(Pageable pageable);

    @Query("SELECT p FROM Periode p WHERE p.statut = 'OUVERTE' ORDER BY p.dateDebut ASC")
    List<Periode> findAllOpen();

    @Query("SELECT p FROM Periode p WHERE p.dateFin < :date AND p.statut != 'CLOTUREE'")
    List<Periode> findExpiredOpenPeriods(@Param("date") LocalDate date);

    boolean existsByDateDebut(LocalDate dateDebut);


    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Periode p WHERE " +
            "(:debut <= p.dateFin AND :fin >= p.dateDebut)")
    boolean existsOverlappingPeriods(@Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    @Query("SELECT p FROM Periode p WHERE p.dateFin < :date AND p.statut != 'CLOTUREE'")
    List<Periode> findExpiredNonCloturees(@Param("date") LocalDate date);
}