package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.api.ProduitLookupApi;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.ProduitRepository;
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
public class ProduitLookupApiImpl implements ProduitLookupApi {

    private final ProduitRepository produitRepository;

    @Override
    public List<Long> findIdsByLibelle(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            return List.of();
        }
        return produitRepository.findIdsByLibelleContainingIgnoreCase(libelle.trim());
    }

    @Override
    public Map<Long, String> findLibellesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return produitRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Produit::getId, Produit::getLibelle));
    }
}