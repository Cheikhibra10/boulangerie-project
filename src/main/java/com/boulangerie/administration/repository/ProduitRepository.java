// administration/repository/ProduitRepository.java
package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends GenericRepository<Produit> {

    /**
     * Recherche un produit par son nom (unique)
     * Utilisé pour : vérification d'unicité (US-09)
     */
    Optional<Produit> findByLibelle(String libelle);

    /**
     * Vérifie si un produit existe avec ce nom
     */
    boolean existsByLibelle(String libelle);

    /**
     * Liste tous les produits actifs
     * Utilisé pour : ventes boutique (US-02), répartition production (US-01)
     */
    List<Produit> findByActifTrue();

    /**
     * Liste les produits actifs par catégorie
     * Utilisé pour : filtrage dans les formulaires
     */
    List<Produit> findByActifTrueAndCategorie(CategorieProduit categorie);

    /**
     * Recherche paginée des produits actifs
     */
    Page<Produit> findByActifTrue(Pageable pageable);

    /**
     * Recherche paginée des produits par catégorie
     */
    Page<Produit> findByActifTrueAndCategorie(CategorieProduit categorie, Pageable pageable);

    /**
     * Recherche les produits dont le prix détail est supérieur à un seuil
     * Utilisé pour : reporting
     */
    List<Produit> findByPrixDetailGreaterThan(BigDecimal seuil);

    /**
     * Recherche un produit actif par ID
     * Utilisé pour : vérification d'existence lors des opérations métier
     */
    Optional<Produit> findByIdAndActifTrue(Long id);

    /**
     * Recherche un produit avec sa catégorie (fetch join pour éviter N+1)
     */
    @Query("SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.id = :id")
    Optional<Produit> findByIdWithCategorie(@Param("id") Long id);

    /**
     * Liste des produits avec leur catégorie (fetch join)
     */
    @Query("SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.actif = true")
    List<Produit> findAllActiveWithCategorie();

    @Query("""
        SELECT p.id
        FROM Produit p
        WHERE p.libelle = :libelle
        """)
    Optional<Long> findIdByLibelle(@Param("libelle") String libelle);
}