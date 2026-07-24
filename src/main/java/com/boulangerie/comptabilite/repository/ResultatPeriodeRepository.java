package com.boulangerie.comptabilite.repository;

import com.boulangerie.comptabilite.model.ResultatPeriode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ResultatPeriodeRepository extends JpaRepository<ResultatPeriode, Long> {

    Optional<ResultatPeriode> findByPeriodeId(Long periodeId);

    @Query("SELECT r FROM ResultatPeriode r JOIN FETCH r.periode ORDER BY r.periode.dateDebut DESC")
    Page<ResultatPeriode> findAllWithPeriode(Pageable pageable);

    @Query("SELECT r FROM ResultatPeriode r JOIN FETCH r.periode WHERE r.periode.id = :periodeId")
    Optional<ResultatPeriode> findByPeriodeIdWithPeriode(@Param("periodeId") Long periodeId);
    @Query("SELECT COALESCE(SUM(r.caAbonnements + r.caVentesLivreurs + r.caVentesBoutique + r.caVenteRestants + r.caAutresProduits), 0) FROM ResultatPeriode r")
    BigDecimal sumCaTotal();

    @Query("SELECT COALESCE(SUM(r.partGerant + r.partBoulangerie), 0) FROM ResultatPeriode r")
    BigDecimal sumBeneficeNet();
    boolean existsByPeriodeId(Long periodeId);
}