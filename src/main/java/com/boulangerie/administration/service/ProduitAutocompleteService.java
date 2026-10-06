package com.boulangerie.administration.service;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final ProduitRepository produitRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Produit>activeAndSearchByField("libelle", q.trim().toLowerCase());
        return produitRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(p -> AutocompleteItemDto.of(
                        p.getId(),
                        p.getLibelle(),
                        p.getTypeProduit() != null ? p.getTypeProduit().name() : null))
                .toList();
    }
}

