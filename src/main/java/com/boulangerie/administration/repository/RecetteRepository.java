package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RecetteRepository extends GenericRepository<Recette>, JpaSpecificationExecutor<Recette> {

    /**
     * Recherche la recette active d'un produit (la plus récente avec actif=true)
     * Utilisé pour : consommation d'ingrédients en production (US-01)
     */
    @Query("SELECT r FROM Recette r " +
            "WHERE r.produit.id = :produitId " +
            "AND r.actif = true " +
            "ORDER BY r.version DESC")
    Optional<Recette> findActiveByProduitId(@Param("produitId") Long produitId);

    /**
     * Recherche la recette active d'un produit avec ses ingrédients (fetch join)
     * Utilisé pour : éviter N+1 lors de la consommation
     */
    @Query("SELECT r FROM Recette r " +
            "JOIN FETCH r.ingredients ri " +
            "JOIN FETCH ri.ingredient i " +
            "WHERE r.produit.id = :produitId " +
            "AND r.actif = true " +
            "ORDER BY r.version DESC")
    Optional<Recette> findActiveByProduitIdWithIngredients(@Param("produitId") Long produitId);

    /**
     * Recherche toutes les versions d'une recette pour un produit (triées par version décroissante)
     * Utilisé pour : historique des recettes (US-09)
     */
    List<Recette> findByProduitIdOrderByVersionDesc(Long produitId);

    /**
     * Recherche la dernière version d'une recette pour un produit (même si inactive)
     */
    @Query("SELECT r FROM Recette r WHERE r.produit.id = :produitId ORDER BY r.version DESC LIMIT 1")
    Optional<Recette> findLatestByProduitId(@Param("produitId") Long produitId);

    /**
     * Vérifie si une recette active existe pour un produit
     */
    boolean existsByProduitIdAndActifTrue(Long produitId);

    /**
     * Liste toutes les recettes actives avec leurs produits associés
     */
    @Query("SELECT r FROM Recette r JOIN FETCH r.produit p WHERE r.actif = true")
    List<Recette> findAllActiveWithProduit();

    /**
     * Vérifie si une combinaison (produit + version) existe déjà
     */
    boolean existsByProduitIdAndVersion(Long produitId, Integer version);

    /**
     * Recherche par produit et version exacte
     */
    Optional<Recette> findByProduitIdAndVersion(Long produitId, Integer version);

    @Query("SELECT r FROM Recette r " +
            "JOIN FETCH r.produit p " +
            "WHERE r.actif = true " +
            "ORDER BY p.libelle")
    Optional<Recette> findByIdWithIngredients(Long id);

    Optional<Recette> findByIdAndVersion(Long recetteId, Integer version);
}