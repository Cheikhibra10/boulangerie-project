package com.boulangerie.livreurs.repository;

import com.boulangerie.livreurs.model.VersementLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersementLivreurRepository extends JpaRepository<VersementLivreur, Long> {
}
