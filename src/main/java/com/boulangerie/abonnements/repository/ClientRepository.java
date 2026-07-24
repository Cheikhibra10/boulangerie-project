package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByPrenomAndNomAndTelephone(String prenom, String nom, String telephone);
    boolean existsByTelephone(String telephone);
}
