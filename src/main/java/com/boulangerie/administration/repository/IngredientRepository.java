package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends GenericRepository<Ingredient> {

    /**
     * Recherche un ingrédient par son libellé
     * Utilisé pour : vérification d'unicité (US-09)
     */
    Optional<Ingredient> findByLibelle(String libelle);

    /**
     * Vérifie si un ingrédient existe avec ce libellé
     */
    boolean existsByLibelle(String libelle);

    /**
     * Liste tous les ingrédients actifs
     * Utilisé pour : sélection dans les recettes (US-11) et les achats (US-06)
     */
    List<Ingredient> findByActifTrue();

    /**
     * Liste les ingrédients actifs avec leur unité (pour affichage)
     */
    List<Ingredient> findByActifTrueOrderByLibelleAsc();

    /**
     * Recherche paginée des ingrédients actifs
     */
    Page<Ingredient> findByActifTrue(Pageable pageable);

    /**
     * Recherche un ingrédient actif par ID
     */
    Optional<Ingredient> findByIdAndActifTrue(Long id);
}