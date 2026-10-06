package com.boulangerie.administration.service;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final IngredientRepository ingredientRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Ingredient>activeAndSearchByField("libelle", q.trim().toLowerCase());
        return ingredientRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(i -> AutocompleteItemDto.of(i.getId(), i.getLibelle()))
                .toList();
    }
}

