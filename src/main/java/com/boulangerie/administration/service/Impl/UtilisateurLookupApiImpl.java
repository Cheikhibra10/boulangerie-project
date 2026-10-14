package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.api.UtilisateurLookupApi;
import com.boulangerie.administration.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurLookupApiImpl implements UtilisateurLookupApi {

    private final UtilisateurRepository utilisateurRepository;
    @Override
    public UtilisateurInfo getByIdOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .map(UtilisateurInfo->new UtilisateurInfo(UtilisateurInfo.getId(), UtilisateurInfo.getNom(), UtilisateurInfo.getPrenom(), UtilisateurInfo.getEmail()))
                .orElseThrow(() -> new RuntimeException("Utilisateur not found with id: " + id));
    }
}
