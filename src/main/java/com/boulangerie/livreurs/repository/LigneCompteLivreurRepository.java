// livreurs/repository/LigneCompteLivreurRepository.java
package com.boulangerie.livreurs.repository;

import com.boulangerie.livreurs.model.LigneCompteLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LigneCompteLivreurRepository extends JpaRepository<LigneCompteLivreur, Long> {

    List<LigneCompteLivreur> findByJournalierId(Long journalierId);

    @Query("SELECT COALESCE(SUM(l.montantApresDeduction), 0) FROM LigneCompteLivreur l WHERE l.journalier.id = :journalierId")
    BigDecimal sumMontantApresDeductionByJournalierId(@Param("journalierId") Long journalierId);
}