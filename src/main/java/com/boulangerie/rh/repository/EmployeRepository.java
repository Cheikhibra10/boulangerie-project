package com.boulangerie.rh.repository;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.rh.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long>,
        JpaSpecificationExecutor<Employe> {
    boolean existsByUtilisateurId(Long utilisateurId);
    boolean existsByTelephone(String telephone);
    Optional<Employe> findByUtilisateurId(Long utilisateurId);
    Optional<Employe> findByTelephone(String telephone);
    @Query("""
        SELECT e.id FROM Employe e
        WHERE LOWER(e.nom) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(e.matricule) LIKE LOWER(CONCAT('%', :q, '%'))
        """)
    List<Long> findIdsByNomOrPrenomOrMatricule(@Param("q") String q);

    Optional<Employe> findByLivreurId(Long livreurId);
}