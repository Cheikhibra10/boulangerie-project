// abonnements/service/AbonnementService.java
package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.*;
import com.boulangerie.abonnements.model.ImportAction;
import com.boulangerie.shared.dto.PageResponse;

public interface AbonnementService {

    // ===== ABONNEMENT =====
    AbonnementDto creerAbonnement(CreationAbonnementDto dto);

    LigneAbonnementDto ajouterClient(Long abonnementId, CreationClientAbonnementDto dto);

    // ===== CONSOMMATION =====
    void enregistrerConsommation(Long ligneId, ConsommationDto dto);

    ImportAction synchroniserConsommation(Long ligneId, ConsommationDto dto);
    // ===== PAIEMENT CLIENT =====
    PaiementClientResultDto enregistrerPaiementClient(Long ligneId, PaiementClientDto dto);

    // ===== VERSEMENT GLOBAL =====
    VersementGlobalResultDto enregistrerVersementGlobal(Long abonnementId, VersementGlobalDto dto);

    // ===== CONSULTATION =====
    AbonnementDto getAbonnement(Long id);

    PageResponse<AbonnementDto> getAbonnements(int page, int size);
}