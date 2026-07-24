// caisse/repository/CaisseRepository.java
package com.boulangerie.comptabilite.repository;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CaisseRepository extends JpaRepository<Caisse, Long> {

    @EntityGraph(attributePaths = {"ouvertePar", "fermeePar"})
    Optional<Caisse> findByStatut(StatutCaisse statut);

//    boolean existsByDateOuvertureAndStatut(LocalDate date, StatutCaisse statut);

    @EntityGraph(attributePaths = {"ouvertePar", "fermeePar"})
    Optional<Caisse> findById(Long id);

    boolean existsByStatut(StatutCaisse statut);
}