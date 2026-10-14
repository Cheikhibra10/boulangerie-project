package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.api.UtilisateurLookupApi;
import com.boulangerie.administration.repository.UtilisateurRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurLookupApiImpl implements UtilisateurLookupApi {

    private final UtilisateurRepository utilisateurRepository;
    @Override
    public UtilisateurInfo getByIdOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .map(UtilisateurInfo->new UtilisateurInfo(UtilisateurInfo.getId(), UtilisateurInfo.getPrenom(), UtilisateurInfo.getNom(), UtilisateurInfo.getEmail(), UtilisateurInfo.getTelephone()))
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur not found with id: " + id));
    }
}
