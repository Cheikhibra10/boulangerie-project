// administration/repository/LivreurRepository.java
package com.boulangerie.administration.repository;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivreurRepository extends GenericRepository<Livreur> {

    /**
     * Recherche tous les livreurs actifs
     * Utilisé pour : sélection dans les listes déroulantes (US-03, US-04)
     */
    List<Livreur> findByActifTrue();

    /**
     * Recherche les livreurs actifs (paginé)
     */
    Page<Livreur> findByActifTrue(Pageable pageable);

    /**
     * Recherche un livreur actif par ID
     * Utilisé pour : validation RG03 (livreur doit être actif pour un abonnement)
     */
    Optional<Livreur> findByIdAndActifTrue(Long id);
}