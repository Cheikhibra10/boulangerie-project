package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ClientAbonnementDto;
import com.boulangerie.abonnements.dto.LigneAbonnementDto;

import java.math.BigDecimal;

public interface LigneAbonnementService {

    LigneAbonnementDto ajouterClient(Long abonnementId, ClientAbonnementDto dto);

    void supprimerClient(Long ligneId);

    LigneAbonnementDto modifierPrix(Long ligneId, BigDecimal prix);

}