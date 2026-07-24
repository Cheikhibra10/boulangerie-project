// ventes/repository/LigneVenteBoutiqueRepository.java
package com.boulangerie.ventes.repository;

import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.TypeVenteLigne;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface LigneVenteBoutiqueRepository extends JpaRepository<LigneVenteBoutique, Long> {

    List<LigneVenteBoutique> findByVenteId(Long venteId);

    @Query("SELECT COALESCE(SUM(l.quantite * l.prixUnitaire), 0) FROM LigneVenteBoutique l WHERE l.vente.id = :venteId")
    BigDecimal sumTotalByVenteId(@Param("venteId") Long venteId);

    List<LigneVenteBoutique> findByVenteIdAndTypeVente(Long venteId, TypeVenteLigne typeVente);

    @Query("SELECT l FROM LigneVenteBoutique l JOIN FETCH l.produitId WHERE l.vente.id = :venteId")
    List<LigneVenteBoutique> findByVenteIdWithProduit(@Param("venteId") Long venteId);

    @Query("SELECT COALESCE(SUM(l.quantite * l.prixUnitaire), 0) FROM LigneVenteBoutique l " +
            "JOIN l.vente v " +
            "WHERE v.date BETWEEN :debut AND :fin AND l.typeVente = :type")
    BigDecimal sumCaBoutiqueBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("type") TypeVenteLigne type
    );

    @Query("""
    SELECT COALESCE(SUM(l.quantite * l.prixUnitaire), 0)
    FROM LigneVenteBoutique l
    JOIN l.vente v
    WHERE v.date BETWEEN :debut AND :fin
      AND l.produitId NOT IN :excludedProductIds
""")
    BigDecimal sumCaAutresProduitsBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("excludedProductIds") Collection<Long> excludedProductIds
    );

    @Query("SELECT COALESCE(SUM(l.quantite), 0) FROM LigneVenteBoutique l " +
            "JOIN l.vente v " +
            "WHERE v.date BETWEEN :debut AND :fin")
    BigDecimal sumQuantiteVendueBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("""
    SELECT l.produitId,
           COALESCE(SUM(l.quantite), 0),
           COALESCE(SUM(l.quantite * l.prixUnitaire), 0)
    FROM LigneVenteBoutique l
    JOIN l.vente v
    WHERE v.date BETWEEN :debut AND :fin
    GROUP BY l.produitId
    ORDER BY COALESCE(SUM(l.quantite * l.prixUnitaire), 0) DESC
""")
    List<Object[]> findTopProduitsByCa(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            Pageable pageable
    );
}