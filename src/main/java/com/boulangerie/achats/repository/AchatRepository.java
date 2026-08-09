package com.boulangerie.achats.repository;

import com.boulangerie.achats.dto.AchatSearchCriteria;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.model.StatutAchat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AchatRepository extends JpaRepository<Achat, Long>, JpaSpecificationExecutor<Achat> {

    @EntityGraph(attributePaths = {"fournisseur", "lignes", "lignes.ingredient"})
    Optional<Achat> findById(Long id);

    @Query("SELECT a FROM Achat a LEFT JOIN FETCH a.lignes l LEFT JOIN FETCH l.ingredient WHERE a.id = :id")
    Optional<Achat> findByIdWithLignesAndIngredients(Long id);

    // Legacy methods (keep for backward compatibility)
    @Deprecated
    Page<Achat> findByFournisseurId(Long fournisseurId, Pageable pageable);

    @Deprecated
    Page<Achat> findByCreatedAtBetween(Instant createdAt, Instant createdAt2, Pageable pageable);


    @Query("""
    SELECT a.statutAchat,
           COUNT(a),
           COALESCE(SUM(a.montantTotal), 0)
    FROM Achat a
    GROUP BY a.statutAchat
    """)
    List<Object[]> countAndSumByStatutAchat();

    @Query("""
    SELECT a.statutPaiement,
           COUNT(a),
           COALESCE(SUM(a.montantPaye), 0)
    FROM Achat a
    GROUP BY a.statutPaiement
    """)
    List<Object[]> countAndSumByStatutPaiement();

    @Query("""
    SELECT a.statutReception,
           COUNT(a),
           COALESCE(SUM(a.montantTotal), 0)
    FROM Achat a
    GROUP BY a.statutReception
    """)
    List<Object[]> countAndSumByStatutReception();

    @Query("SELECT COALESCE(SUM(a.montantTotal), 0) FROM Achat a WHERE a.statutAchat = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") StatutAchat statut);
}