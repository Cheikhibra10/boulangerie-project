// abonnements/repository/VersementAbonnementRepository.java
package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.VersementAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersementAbonnementRepository extends JpaRepository<VersementAbonnement, Long> {
}