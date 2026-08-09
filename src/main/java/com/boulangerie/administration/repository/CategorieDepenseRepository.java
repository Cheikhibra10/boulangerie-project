package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieDepenseRepository extends GenericRepository<CategorieDepense> {

    /**
     * Recherche une catégorie par son libellé
     */
    Optional<CategorieDepense> findByLibelle(String libelle);

    /**
     * Vérifie si une catégorie existe avec ce libellé
     */
    boolean existsByLibelle(String libelle);

    /**
     * Liste toutes les catégories actives
     * Utilisé pour : formulaires de saisie de dépenses (US-08)
     */
    List<CategorieDepense> findByActifTrue();
}