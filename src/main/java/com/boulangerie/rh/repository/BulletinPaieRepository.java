package com.boulangerie.rh.repository;

import com.boulangerie.rh.dto.MasseSalarialeAggregate;
import com.boulangerie.rh.model.BulletinPaie;
import com.boulangerie.rh.model.StatutBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BulletinPaieRepository
        extends JpaRepository<BulletinPaie, Long>,
        JpaSpecificationExecutor<BulletinPaie> {

    boolean existsByEmployeIdAndPeriodeId(Long employeId, Long periodeId);

    @Query("""
    SELECT 
        COUNT(b)                    AS nombre,
        COALESCE(SUM(b.salaireBrut), 0) AS totalBrut,
        COALESCE(SUM(b.retenues), 0)    AS totalRetenues,
        COALESCE(SUM(b.netAPayer), 0)   AS totalNet
    FROM BulletinPaie b
    WHERE b.periodeId = :periodeId
      AND b.statut IN :statuts
    """)
    MasseSalarialeProjection aggregateMasseSalariale(
            @Param("periodeId") Long periodeId,
            @Param("statuts") Collection<StatutBulletin> statuts
    );
}