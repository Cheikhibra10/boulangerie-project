package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieProduitRepository extends GenericRepository<CategorieProduit> {

    /**
     * Recherche une catégorie par son libellé
     * Utilisé pour : vérification d'unicité (US-09)
     */
    Optional<CategorieProduit> findByLibelle(String libelle);

    /**
     * Vérifie si une catégorie existe avec ce libellé
     */
    boolean existsByLibelle(String libelle);

    /**
     * Liste toutes les catégories actives
     * Utilisé pour : formulaires de création de produits
     */
    List<CategorieProduit> findByActifTrue();
}