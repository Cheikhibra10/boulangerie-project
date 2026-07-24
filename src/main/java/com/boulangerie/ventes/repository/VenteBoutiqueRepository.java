// ventes/repository/VenteBoutiqueRepository.java
package com.boulangerie.ventes.repository;

import com.boulangerie.ventes.model.VenteBoutique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VenteBoutiqueRepository extends JpaRepository<VenteBoutique, Long> {

    Optional<VenteBoutique> findByDateAndCaisseId(LocalDate date, Long caisseId);

    boolean existsByDateAndCaisseId(LocalDate date, Long caisseId);

    Page<VenteBoutique> findByDate(LocalDate date, Pageable pageable);

    @EntityGraph(attributePaths = {"caisse", "utilisateur"})
    @Query("SELECT v FROM VenteBoutique v WHERE v.id = :id")
    Optional<VenteBoutique> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT v.date, COALESCE(SUM(l.quantite * l.prixUnitaire), 0) " +
            "FROM VenteBoutique v " +
            "JOIN v.lignes l " +
            "WHERE v.date BETWEEN :debut AND :fin " +
            "GROUP BY v.date " +
            "ORDER BY v.date ASC")
    List<Object[]> findCaByDateBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}