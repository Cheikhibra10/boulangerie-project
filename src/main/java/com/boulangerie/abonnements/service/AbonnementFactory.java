package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.CreationAbonnementDto;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.model.CompteAbonnement;
import com.boulangerie.abonnements.repository.CompteAbonnementRepository;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// abonnements/application/service/AbonnementFactory.java
@Component
@RequiredArgsConstructor
public class AbonnementFactory {

    private final CompteAbonnementRepository compteRepository;

    public Abonnement create(CreationAbonnementDto dto, Long livreurId) {
        verifierDates(dto);
        Abonnement abonnement = new Abonnement()
                .setNom(dto.getNom())
                .setAdresse(dto.getAdresse())
                .setDateDebut(dto.getDateDebut())
                .setDateFin(dto.getDateFin())
                .setLivreurId(livreurId)
                .setActif(true);

        CompteAbonnement compte = new CompteAbonnement()
                .setAbonnement(abonnement)
                .setSoldeActuel(BigDecimal.ZERO);
        abonnement.setCompte(compte);
        return abonnement;
    }

    private void verifierDates(CreationAbonnementDto dto) {
        if (dto.getDateFin() != null && dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new BadRequestException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}