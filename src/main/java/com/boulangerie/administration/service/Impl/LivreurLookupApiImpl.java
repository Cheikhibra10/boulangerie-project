package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.api.LivreurLookupApi;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.repository.LivreurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LivreurLookupApiImpl implements LivreurLookupApi {
    private final LivreurRepository livreurRepository;
    @Override
    public List<Long> findIdsByNom(String nom) {
        if(nom == null || nom.trim().isEmpty()) {
            return List.of();
        }
        // Implementation for finding IDs by name
        return livreurRepository.findIdsByNomOrPrenomContainingIgnoreCase(nom);
    }

    @Override
    public Map<Long, String> findNomsByIds(Collection<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            return Map.of();
        }
       return livreurRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Livreur::getId, livreur -> livreur.getNom() + " " + livreur.getPrenom()));
    }
}
