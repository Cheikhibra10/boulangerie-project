package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.Client;
import com.boulangerie.abonnements.repository.ClientRepository;
import com.boulangerie.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ClientFactory {


    public Client create(
            String nom,
            String prenom,
            String telephone
    ) {

        return new Client()
                .setNom(nom)
                .setPrenom(prenom)
                .setTelephone(telephone)
                .setActif(true);
    }
}