package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.abonnements.projection.LigneAbonnementReportProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneAbonnementRepository extends JpaRepository<LigneAbonnement, Long> {

    Optional<LigneAbonnement> findByAbonnementIdAndClientId(Long abonnementId, Long clientId);

    List<LigneAbonnement> findByAbonnementId(Long abonnementId);

    @Query("SELECT l FROM LigneAbonnement l JOIN FETCH l.client WHERE l.abonnement.id = :abonnementId AND l.reliquat > 0 ORDER BY l.id")
    List<LigneAbonnement> findClientsEndettesByAbonnementId(@Param("abonnementId") Long abonnementId);

    @Query("""
    SELECT
        l.id AS ligneId,
        a.id AS abonnementId,
        c.id AS clientId,
        c.nom AS clientNom,
        c.prenom AS clientPrenom,
        l.prixUnitaire AS prixUnitaire,
        l.reliquat AS reliquat
    FROM LigneAbonnement l
    JOIN l.abonnement a
    JOIN l.client c
    WHERE a.id IN :abonnementId
    ORDER BY a.id, l.id
""")
    List<LigneAbonnementReportProjection> findReportData(
            @Param("abonnementId") Long abonnementIds
    );

    @Query("""
    SELECT
        l.id AS ligneId,
        a.id AS abonnementId,
        c.id AS clientId,
        c.nom AS clientNom,
        c.prenom AS clientPrenom,
        l.prixUnitaire AS prixUnitaire,
        l.reliquat AS reliquat
    FROM LigneAbonnement l
    JOIN l.abonnement a
    JOIN l.client c
    WHERE a.id IN :abonnementIds
    ORDER BY a.id, l.id
""")
    List<LigneAbonnementReportProjection> findReportData(
            @Param("abonnementIds") List<Long> abonnementIds
    );
}
