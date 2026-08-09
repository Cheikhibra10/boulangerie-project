package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepository extends GenericRepository<Fournisseur> {

    /**
     * Recherche un fournisseur par son nom
     * Utilisé pour : vérification d'unicité (US-09)
     */
    Optional<Fournisseur> findByNom(String nom);

    /**
     * Vérifie si un fournisseur existe avec ce nom
     */
    boolean existsByNom(String nom);

    /**
     * Liste tous les fournisseurs actifs
     * Utilisé pour : sélection dans les commandes d'achat (US-06)
     */
    List<Fournisseur> findByActifTrue();

    /**
     * Recherche les fournisseurs actifs (paginé)
     */
    Page<Fournisseur> findByActifTrue(Pageable pageable);

    /**
     * Recherche un fournisseur actif par ID
     */
    Optional<Fournisseur> findByIdAndActifTrue(Long id);
}