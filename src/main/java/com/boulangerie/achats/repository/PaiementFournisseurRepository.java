package com.boulangerie.achats.repository;

import com.boulangerie.achats.model.PaiementFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface PaiementFournisseurRepository extends JpaRepository<PaiementFournisseur, Long> {

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PaiementFournisseur p WHERE p.achat.id = :achatId")
    BigDecimal sumMontantByAchatId(@Param("achatId") Long achatId);
}