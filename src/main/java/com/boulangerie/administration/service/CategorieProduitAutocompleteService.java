package com.boulangerie.administration.service;

import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.repository.CategorieProduitRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieProduitAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final CategorieProduitRepository categorieProduitRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<CategorieProduit>activeAndSearchByField("libelle", q.trim().toLowerCase());
        return categorieProduitRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(c -> AutocompleteItemDto.of(c.getId(), c.getLibelle()))
                .toList();
    }
}

