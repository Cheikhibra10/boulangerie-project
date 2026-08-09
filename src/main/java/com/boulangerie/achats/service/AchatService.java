package com.boulangerie.achats.service;

import com.boulangerie.achats.dto.*;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface AchatService {
    AchatDto creerAchat(CreationAchatDto dto);
    LigneAchatDto ajouterLigne(Long achatId, LigneAchatRequestDto dto);
    LigneAchatDto modifierLigne(Long ligneId, LigneAchatRequestDto dto);

    void supprimerLigne(Long ligneId);
    AchatDto retournerAchat(Long achatId, RetourAchatRequestDto dto);
    AchatDto recevoirAchat(Long achatId, ReceptionAchatRequestDto dto);
    PaiementFournisseurDto enregistrerPaiement(Long achatId, PaiementFournisseurRequestDto dto);
    AchatDto annulerAchat(Long achatId);
    AchatDto getAchat(Long id);
    List<LigneAchatDto> getLignesByAchat(Long achatId);

    PageResponse<AchatDto> getAchats(AchatSearchRequest request, int page, int size);
}