// abonnements/repository/CompteAbonnementRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.CompteAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompteAbonnementRepository extends JpaRepository<CompteAbonnement, Long> {
    Optional<CompteAbonnement> findByAbonnementId(Long abonnementId);
}