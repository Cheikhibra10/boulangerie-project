package com.boulangerie.ventes.repository;

import com.boulangerie.ventes.service.TopProduitProjection;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.StatutVente;
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

    /*
     * Pour le rapport mensuel VERSEMENTS/FRAIS : lignes de vente
     * payées sur la période, pour classification PAIN / autres
     * produits jour par jour (cf. VersementsReportingService).
     */
    List<LigneVenteBoutique> findByVenteDateBetweenAndVenteStatut(
            LocalDate debut,
            LocalDate fin,
            StatutVente statut
    );

    @Query("SELECT l FROM LigneVenteBoutique l JOIN FETCH l.produitId WHERE l.vente.id = :venteId")
    List<LigneVenteBoutique> findByVenteIdWithProduit(@Param("venteId") Long venteId);

    @Query("""
    SELECT COALESCE(
        SUM((l.quantite - COALESCE(l.quantiteRetournee, 0)) * l.prixUnitaire),
        0
    )
    FROM LigneVenteBoutique l
    JOIN l.vente v
    WHERE v.date BETWEEN :debut AND :fin
      AND l.typeVente = :type
""")
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
  AND l.produitId NOT IN (:painIds)
""")
    BigDecimal sumCaAutresProduits(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("painIds") Collection<Long> painIds);

    @Query("SELECT COALESCE(SUM(l.quantite - l.quantiteRetournee), 0) FROM LigneVenteBoutique l " +
            "JOIN l.vente v " +
            "WHERE v.date BETWEEN :debut AND :fin")
    BigDecimal sumQuantiteVendueBetweenDates(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    @Query("""
SELECT
    l.produitId AS produitId,
    SUM(l.quantite) AS quantiteVendue,
    SUM(l.quantite * l.prixUnitaire) AS caTotal
FROM LigneVenteBoutique l
JOIN l.vente v
WHERE v.date BETWEEN :debut AND :fin
GROUP BY l.produitId
ORDER BY SUM(l.quantite * l.prixUnitaire) DESC
""")
    List<TopProduitProjection> findTopProduitsByCa(
            LocalDate debut,
            LocalDate fin,
            Pageable pageable);
}