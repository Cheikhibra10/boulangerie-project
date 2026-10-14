package com.boulangerie.rh.repository;

import com.boulangerie.rh.model.AvanceSalaire;
import com.boulangerie.rh.model.StatutAvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AvanceSalaireRepository
        extends JpaRepository<AvanceSalaire, Long>,
                JpaSpecificationExecutor<AvanceSalaire> {

    List<AvanceSalaire> findByEmployeIdAndStatut(Long employeId, StatutAvance statut);

    @Query("""
            SELECT COALESCE(SUM(a.montant), 0)
            FROM AvanceSalaire a
            WHERE a.employe.id = :employeId
              AND a.statut = :statut
            """)
    BigDecimal sumMontantApprouvees(@Param("employeId") Long employeId, @Param("statut") StatutAvance statut);
}