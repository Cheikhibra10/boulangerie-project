package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.LigneAbonnement;
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
}