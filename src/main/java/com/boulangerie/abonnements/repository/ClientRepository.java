package com.boulangerie.abonnements.repository;

import com.boulangerie.abonnements.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
    boolean existsByPrenomAndNomAndTelephone(String prenom, String nom, String telephone);
    boolean existsByTelephone(String telephone);
}
