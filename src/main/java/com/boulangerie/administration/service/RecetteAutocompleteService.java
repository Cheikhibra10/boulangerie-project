package com.boulangerie.administration.service;

import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.repository.RecetteRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecetteAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final RecetteRepository recetteRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Recette>activeAndSearchByField("produit.libelle", q.trim().toLowerCase());
        return recetteRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(r -> AutocompleteItemDto.of(
                        r.getId(),
                        r.getProduit() != null ? r.getProduit().getLibelle() : null,
                        r.getProduit() != null ? r.getProduit().getLibelle() : null))
                .toList();
    }
}

