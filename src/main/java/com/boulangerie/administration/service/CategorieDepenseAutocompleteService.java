package com.boulangerie.administration.service;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.administration.repository.CategorieDepenseRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieDepenseAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final CategorieDepenseRepository categorieDepenseRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<CategorieDepense>activeAndSearchByField("libelle", q.trim().toLowerCase());
        return categorieDepenseRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(c -> AutocompleteItemDto.of(c.getId(), c.getLibelle()))
                .toList();
    }
}

