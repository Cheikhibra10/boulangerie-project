package com.boulangerie.livreurs.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.livreurs.model.CommissionRegle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CommissionRegleRepository extends JpaRepository<CommissionRegle, Long> {

    @Query("""
        SELECT c
        FROM CommissionRegle c
        WHERE c.livreurId = :livreurId
          AND c.produitId = :produitId
          AND c.dateDebut <= :date
          AND (c.dateFin IS NULL OR c.dateFin >= :date)
    """)
    Optional<CommissionRegle> findActiveRule(
            Long livreurId,
            Long produitId,
            LocalDate date
    );

    Optional<CommissionRegle> findTopByLivreurIdAndProduitIdOrderByDateDebutDesc(
            Long livreurId,
            Long produitId
    );

    boolean existsByLivreurIdAndProduitIdAndDateFinIsNull(
            Long livreurId,
            Long produitId
    );

}